package sm.domain.sys.scheduler.constant;

/** 定时任务管理权限码，统一供接口鉴权和权限审计使用。 */
public final class JobPermission {
    public static final String LIST = "sys:scheduler:job:listPage";
    public static final String DETAIL = "sys:scheduler:job:detail";
    public static final String SAVE = "sys:scheduler:job:save";
    public static final String DELETE = "sys:scheduler:job:delete";

    private JobPermission() {
    }
}
