package sm.domain.sys.monitor.sql.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;
import sm.domain.sys.monitor.sql.model.vo.SqlColumnVO;
import sm.domain.sys.monitor.sql.model.vo.SqlResultVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** SQL 执行与专用审计的原子事务边界。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class SqlExecutionTxService {
    private static final int QUERY_TIMEOUT_SECONDS = 30;
    private final DataSource dataSource;
    private final SqlLogMapper sqlLogMapper;

    SqlResultVO execute(SqlExecutionPlan plan, int maxRows, SqlLogEntity logEntity) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        Savepoint executionStart = null;
        Instant start = Instant.now();
        SqlResultVO result;
        try {
            executionStart = connection.setSavepoint("sql_console_execution");
            result = plan.statements().size() > 1
                    ? executeInsertBatch(connection, plan)
                    : executeSingle(connection, plan, maxRows);
        } catch (SQLException exception) {
            rollbackExecution(connection, executionStart);
            result = errorResult(plan, exception);
        }
        result.setExecuteDuration(Math.toIntExact(Duration.between(start, Instant.now()).toMillis()));
        completeLog(logEntity, result, plan.type());
        if (sqlLogMapper.insert(logEntity) != 1) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "SQL 执行审计写入失败");
        }
        return result;
    }

    private SqlResultVO executeSingle(Connection connection, SqlExecutionPlan plan, int maxRows) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            if ("QUERY".equals(plan.type())) {
                statement.setMaxRows(maxRows + 1);
            }
            boolean hasResultSet = statement.execute(plan.statements().getFirst());
            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    return mapQueryResult(resultSet, maxRows);
                }
            }
            SqlResultVO result = new SqlResultVO();
            int affectedRows = Math.max(statement.getUpdateCount(), 0);
            result.setType(plan.type());
            result.setRowCount(affectedRows);
            result.setStatementCount(1);
            result.setStatementRowCounts(List.of(affectedRows));
            result.setMessage(affectedRows + " 行受影响");
            return result;
        }
    }

    private SqlResultVO executeInsertBatch(Connection connection, SqlExecutionPlan plan) throws SQLException {
        List<Integer> rowCounts = new ArrayList<>(plan.statements().size());
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            for (String sql : plan.statements()) {
                boolean hasResultSet = statement.execute(sql);
                if (hasResultSet) {
                    throw new SQLException("批量 INSERT 不允许返回查询结果");
                }
                rowCounts.add(Math.max(statement.getUpdateCount(), 0));
            }
        }
        int totalRows = rowCounts.stream().mapToInt(Integer::intValue).sum();
        SqlResultVO result = new SqlResultVO();
        result.setType("DML");
        result.setRowCount(totalRows);
        result.setStatementCount(plan.statements().size());
        result.setStatementRowCounts(rowCounts);
        result.setMessage(plan.statements().size() + " 条 INSERT 执行成功，共影响 " + totalRows + " 行");
        return result;
    }

    private SqlResultVO mapQueryResult(ResultSet resultSet, int maxRows) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        Map<SourceColumn, String> columnComments = resolveColumnComments(resultSet.getStatement().getConnection(), metadata);
        List<SqlColumnVO> columns = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            SourceColumn sourceColumn = sourceColumn(metadata, columnIndex);
            String comment = sourceColumn == null ? "" : columnComments.getOrDefault(sourceColumn, "");
            columns.add(new SqlColumnVO("column_" + columnIndex, metadata.getColumnLabel(columnIndex),
                    metadata.getColumnTypeName(columnIndex), comment));
        }
        List<List<Object>> rows = new ArrayList<>();
        boolean truncated = false;
        while (resultSet.next()) {
            if (rows.size() == maxRows) {
                truncated = true;
                break;
            }
            List<Object> row = new ArrayList<>(columnCount);
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                row.add(toJsonValue(resultSet.getObject(columnIndex)));
            }
            rows.add(row);
        }
        SqlResultVO result = new SqlResultVO();
        result.setType("QUERY");
        result.setColumns(columns);
        result.setRows(rows);
        result.setRowCount(rows.size());
        result.setTruncated(truncated);
        result.setStatementCount(1);
        result.setMessage(truncated ? "已返回前 " + maxRows + " 行" : "查询成功");
        return result;
    }

    /**
     * 使用结果集元数据定位源列，并通过一次 pg_catalog 查询批量取得注释。
     * 表达式、聚合列和无法定位源表的列保持空注释。
     */
    private Map<SourceColumn, String> resolveColumnComments(Connection connection, ResultSetMetaData metadata)
            throws SQLException {
        List<SourceColumn> sourceColumns = new ArrayList<>();
        for (int columnIndex = 1; columnIndex <= metadata.getColumnCount(); columnIndex++) {
            SourceColumn sourceColumn = sourceColumn(metadata, columnIndex);
            if (sourceColumn != null && !sourceColumns.contains(sourceColumn)) {
                sourceColumns.add(sourceColumn);
            }
        }
        if (sourceColumns.isEmpty()) {
            return Map.of();
        }
        String valuesClause = String.join(", ", java.util.Collections.nCopies(sourceColumns.size(), "(?, ?, ?)"));
        String query = "SELECT source.schema_name, source.table_name, source.column_name, "
                + "col_description(table_class.oid, attribute.attnum) "
                + "FROM (VALUES " + valuesClause + ") AS source(schema_name, table_name, column_name) "
                + "JOIN pg_namespace namespace ON namespace.nspname = source.schema_name "
                + "JOIN pg_class table_class ON table_class.relnamespace = namespace.oid "
                + "AND table_class.relname = source.table_name "
                + "JOIN pg_attribute attribute ON attribute.attrelid = table_class.oid "
                + "AND attribute.attname = source.column_name";
        Map<SourceColumn, String> comments = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int parameterIndex = 1;
            for (SourceColumn sourceColumn : sourceColumns) {
                statement.setString(parameterIndex++, sourceColumn.schemaName());
                statement.setString(parameterIndex++, sourceColumn.tableName());
                statement.setString(parameterIndex++, sourceColumn.columnName());
            }
            try (ResultSet commentResult = statement.executeQuery()) {
                while (commentResult.next()) {
                    String comment = commentResult.getString(4);
                    if (comment != null) {
                        comments.put(new SourceColumn(commentResult.getString(1), commentResult.getString(2),
                                commentResult.getString(3)), comment);
                    }
                }
            }
        }
        return comments;
    }

    private SourceColumn sourceColumn(ResultSetMetaData metadata, int columnIndex) throws SQLException {
        String tableName = metadata.getTableName(columnIndex);
        String columnName = metadata.getColumnName(columnIndex);
        if (tableName == null || tableName.isBlank() || columnName == null || columnName.isBlank()) {
            return null;
        }
        String schemaName = metadata.getSchemaName(columnIndex);
        return new SourceColumn(schemaName == null || schemaName.isBlank() ? "public" : schemaName,
                tableName, columnName);
    }

    private Object toJsonValue(Object value) throws SQLException {
        if (value instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        if (value instanceof Array sqlArray) {
            try {
                Object arrayValue = sqlArray.getArray();
                return arrayValue instanceof Object[] values ? Arrays.asList(values) : String.valueOf(arrayValue);
            } finally {
                sqlArray.free();
            }
        }
        // PostgreSQL 的 json/jsonb、inet 等扩展类型由 PGobject 承载，只返回其文本值。
        if ("org.postgresql.util.PGobject".equals(value == null ? null : value.getClass().getName())) {
            return value.toString();
        }
        return value;
    }

    private SqlResultVO errorResult(SqlExecutionPlan plan, SQLException exception) {
        SqlResultVO result = new SqlResultVO();
        result.setType("ERROR");
        result.setRowCount(0);
        result.setStatementCount(plan.statements().size());
        result.setMessage(exception.getMessage());
        return result;
    }

    private void rollbackExecution(Connection connection, Savepoint savepoint) {
        try {
            if (savepoint != null) {
                connection.rollback(savepoint);
            }
        } catch (SQLException rollbackException) {
            throw new BizException(ResultEnum.SQL_ERROR, "SQL 执行失败且事务回滚失败");
        }
    }

    private void completeLog(SqlLogEntity logEntity, SqlResultVO result, String plannedType) {
        logEntity.setResultType("ERROR".equals(result.getType()) ? "ERROR" : plannedType);
        logEntity.setExecuteDuration(result.getExecuteDuration());
        logEntity.setRowCount(result.getRowCount());
        logEntity.setErrorMessage("ERROR".equals(result.getType()) ? result.getMessage() : null);
    }

    private record SourceColumn(String schemaName, String tableName, String columnName) {
    }
}
