package sm.domain.sys.monitor.sql.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.UserHelper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;
import sm.domain.sys.monitor.sql.model.form.SqlExecuteForm;
import sm.domain.sys.monitor.sql.model.form.SqlLogListForm;
import sm.domain.sys.monitor.sql.model.vo.SqlLogDetailVO;
import sm.domain.sys.monitor.sql.model.vo.SqlLogListVO;
import sm.domain.sys.monitor.sql.model.vo.SqlResultVO;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.util.ServletUtil;
import sm.system.util.StringUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL 控制台服务——JDBC 直接执行 SQL + 审计日志持久化
 */
@Slf4j
@Service
public class SqlService {

    private final DataSource dataSource;
    private final SqlLogMapper mapper;
    private final SqlLogTxService txService;
    private final SqlLogConverter converter;

    public SqlService(
            DataSource dataSource,
            SqlLogMapper mapper,
            SqlLogTxService txService,
            SqlLogConverter converter) {
        this.dataSource = dataSource;
        this.mapper = mapper;
        this.txService = txService;
        this.converter = converter;
    }

    @BizLog(value = "执行SQL", saveRequest = false, saveResponse = false)
    public SqlResultVO execute(SqlExecuteForm form) {
        UserHelper.checkAdmin();
        String sql = form.getSql().trim();
        if (sql.isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "SQL 语句不能为空");
        }

        long start = System.currentTimeMillis();
        String resultType;
        SqlResultVO result = new SqlResultVO();

        // 检测 SQL 类型，QUERY 且无 LIMIT 则追加
        resultType = detectSqlType(sql);
        String execSql = sql;
        if ("QUERY".equals(resultType) && !sql.toUpperCase().contains("LIMIT")) {
            execSql = sql + " LIMIT 500";
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(true);
            stmt.setQueryTimeout(30);

            boolean hasResultSet = stmt.execute(execSql);

            if (hasResultSet) {
                // SELECT 查询
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    List<String> columns = new ArrayList<>(colCount);
                    List<String> comments = new ArrayList<>(colCount);
                    for (int i = 1; i <= colCount; i++) {
                        columns.add(meta.getColumnLabel(i));
                        comments.add(resolveColumnComment(conn, meta, i));
                    }

                    List<Map<String, Object>> rows = new ArrayList<>();
                    int rowCount = 0;
                    while (rs.next() && rowCount < 500) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= colCount; i++) {
                            row.put(columns.get(i - 1), rs.getObject(i));
                        }
                        rows.add(row);
                        rowCount++;
                    }

                    result.setType("QUERY");
                    result.setColumns(columns);
                    result.setComments(comments);
                    result.setRows(rows);
                    result.setRowCount(rowCount);
                }
            } else {
                // DML / DDL
                int affected = stmt.getUpdateCount();
                result.setType(resultType);
                result.setRowCount(affected);
                result.setMessage(affected + " 行受影响");
            }

        } catch (SQLException e) {
            log.warn("SQL 执行异常: {}", e.getMessage());
            result.setType("ERROR");
            result.setMessage(e.getMessage());
        }

        result.setExecuteDuration((int) (System.currentTimeMillis() - start));

        // 持久化审计日志
        saveLog(sql, resultType, result);

        return result;
    }

    /**
     * 分页查询执行历史
     */
    public PageData<SqlLogListVO> listPage(SqlLogListForm form) {
        UserHelper.checkAdmin();
        LambdaQueryWrapper<SqlLogEntity> qw = new LambdaQueryWrapper<SqlLogEntity>();
        if (StringUtil.isNotBlank(form.getKeyword())) {
            qw.like(SqlLogEntity::getSqlText, form.getKeyword());
        }
        if (StringUtil.isNotBlank(form.getResultType())) {
            qw.eq(SqlLogEntity::getResultType, form.getResultType());
        }
        qw.orderByDesc(SqlLogEntity::getId);

        Page<SqlLogEntity> page = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), qw);
        List<SqlLogListVO> vos = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), vos);
    }

    /**
     * 查询单条执行历史
     */
    public SqlLogEntity detail(Long id) {
        UserHelper.checkAdmin();
        SqlLogEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "执行日志不存在");
        }
        return entity;
    }

    public SqlLogDetailVO getDetail(Long id) {
        UserHelper.checkAdmin();
        SqlLogEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "执行日志不存在");
        }
        return converter.toDetailVO(entity);
    }

    // ---- 私有辅助方法 ----

    private String detectSqlType(String sql) {
        String upper = sql.toUpperCase().trim();
        if (upper.startsWith("SELECT") || upper.startsWith("WITH")) return "QUERY";
        if (upper.startsWith("INSERT") || upper.startsWith("UPDATE") || upper.startsWith("DELETE")) return "DML";
        if (upper.startsWith("CREATE") || upper.startsWith("ALTER")
                || upper.startsWith("DROP") || upper.startsWith("TRUNCATE")
                || upper.startsWith("COMMENT")) return "DDL";
        return "OTHER";
    }

    /**
     * 通过 pg_catalog 获取列注释，解析失败返回空字符串
     */
    private String resolveColumnComment(Connection conn, ResultSetMetaData meta, int colIndex) throws SQLException {
        try {
            String tableName = meta.getTableName(colIndex);
            if (tableName == null || tableName.isEmpty()) {
                return "";
            }
            String columnName = meta.getColumnName(colIndex);
            if (columnName == null || columnName.isEmpty()) {
                return "";
            }

            String sql = "SELECT col_description(c.oid, a.attnum) FROM pg_class c"
                    + " JOIN pg_attribute a ON a.attrelid = c.oid"
                    + " WHERE c.relname = ? AND a.attname = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ps.setString(2, columnName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String comment = rs.getString(1);
                        return comment != null ? comment : "";
                    }
                }
            }
        } catch (SQLException e) {
            log.debug("解析列注释失败 colIndex={}: {}", colIndex, e.getMessage());
        }
        return "";
    }

    private void saveLog(String sql, String resultType, SqlResultVO result) {
        try {
            SqlLogEntity logEntity = new SqlLogEntity();
            logEntity.setSqlText(sql);
            logEntity.setResultType("ERROR".equals(result.getType()) ? result.getType() : resultType);
            logEntity.setExecuteDuration(result.getExecuteDuration());
            logEntity.setRowCount(result.getRowCount());
            logEntity.setErrorMessage("ERROR".equals(result.getType()) ? result.getMessage() : null);

            if (UserHelper.isLogin()) {
                logEntity.setCreateName(UserHelper.getCurrentUser().getUsername());
            }
            try {
                logEntity.setCreateIp(ServletUtil.getClientIp());
            } catch (Exception ignored) {
                // 非 Web 上下文时忽略
            }

            txService.save(logEntity);
        } catch (Exception e) {
            log.warn("保存 SQL 执行日志失败: {}", e.getMessage());
        }
    }
}
