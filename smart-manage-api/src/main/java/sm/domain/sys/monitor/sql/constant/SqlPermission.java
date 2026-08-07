package sm.domain.sys.monitor.sql.constant;

/** SQL 控制台权限码，统一供接口鉴权和权限审计使用。 */
public final class SqlPermission {
    public static final String EXECUTE = "sys:monitor:sql:execute";
    public static final String LOG_LIST = "sys:monitor:sql:log:listPage";
    public static final String LOG_DETAIL = "sys:monitor:sql:log:detail";

    private SqlPermission() {
    }
}
