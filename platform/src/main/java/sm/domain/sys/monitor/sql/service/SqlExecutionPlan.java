package sm.domain.sys.monitor.sql.service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/** 解析并固定 SQL 控制台允许的执行边界。 */
record SqlExecutionPlan(String type, List<String> statements) {
    private static final int MAX_INSERT_STATEMENTS = 100;

    static SqlExecutionPlan parse(String sql) {
        try {
            List<Statement> parsed = CCJSqlParserUtil.parseStatements(sql).getStatements();
            if (parsed.isEmpty()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "SQL 语句不能为空");
            }
            if (parsed.size() > MAX_INSERT_STATEMENTS) {
                throw new BizException(ResultEnum.PARAM_ERROR, "批量 INSERT 不能超过 100 条");
            }
            if (parsed.size() > 1 && parsed.stream().anyMatch(statement -> !(statement instanceof Insert))) {
                throw new BizException(ResultEnum.PARAM_ERROR, "多语句执行仅允许全部为 INSERT");
            }
            Statement first = parsed.getFirst();
            String type = classify(first);
            List<String> executableStatements = parsed.size() == 1
                    ? List.of(sql)
                    : parsed.stream().map(Statement::toString).toList();
            return new SqlExecutionPlan(type, executableStatements);
        } catch (JSQLParserException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "SQL 语法无法解析：" + exception.getMessage());
        }
    }

    private static String classify(Statement statement) {
        if (statement instanceof Select) {
            return "QUERY";
        }
        if (statement instanceof Insert || statement instanceof Update || statement instanceof Delete) {
            return "DML";
        }
        String simpleName = statement.getClass().getSimpleName();
        if (List.of("CreateTable", "CreateIndex", "CreateSchema", "CreateView", "Alter", "Drop", "Truncate", "Comment")
                .contains(simpleName)) {
            return "DDL";
        }
        throw new BizException(ResultEnum.PARAM_ERROR, "不支持执行该类型的 SQL 语句：" + simpleName);
    }
}
