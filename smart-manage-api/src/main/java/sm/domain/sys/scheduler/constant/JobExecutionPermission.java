package sm.domain.sys.scheduler.constant;

/** 任务执行实例权限码，统一供接口鉴权和权限审计使用。 */
public final class JobExecutionPermission {
    public static final String LIST = "sys:scheduler:execution:listPage";
    public static final String DETAIL = "sys:scheduler:execution:detail";

    private JobExecutionPermission() {
    }
}
