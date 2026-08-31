package sm.domain.sys.scheduler.job;

import java.util.List;

/** 系统日志分区白名单，禁止任务参数传入任意表名执行 DDL。 */
enum SystemLogPartitionDefinition {
    LOGIN("loginLog", "t_sys_login_log", "t_sys_login_log_history", false),
    OPERATE("operateLog", "t_sys_operate_log", "t_sys_operate_log_history", false),
    SQL("sqlLog", "t_sys_sql_log", "t_sys_sql_log_history", false),
    SCRIPT("scriptLog", "t_sys_script_log", "t_sys_script_log_history", false),
    JOB("jobLog", "t_sys_job_log", "t_sys_job_log_history", true),
    OPENAPI("openApiLog", "t_sys_openapi_invocation_log",
            "t_sys_openapi_invocation_log_history", false);

    static final List<SystemLogPartitionDefinition> VALUES = List.of(values());

    private final String parameterPrefix;
    private final String activeTable;
    private final String historyTable;
    private final boolean protectUnsettledExecutions;

    SystemLogPartitionDefinition(String parameterPrefix, String activeTable, String historyTable,
                                 boolean protectUnsettledExecutions) {
        this.parameterPrefix = parameterPrefix;
        this.activeTable = activeTable;
        this.historyTable = historyTable;
        this.protectUnsettledExecutions = protectUnsettledExecutions;
    }

    String parameterPrefix() {
        return parameterPrefix;
    }

    String activeTable() {
        return activeTable;
    }

    String historyTable() {
        return historyTable;
    }

    boolean protectUnsettledExecutions() {
        return protectUnsettledExecutions;
    }
}
