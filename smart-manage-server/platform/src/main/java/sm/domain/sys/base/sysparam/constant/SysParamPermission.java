package sm.domain.sys.base.sysparam.constant;

/** 系统参数权限码，统一供接口鉴权和权限审计使用。 */
public final class SysParamPermission {
    public static final String LIST = "sys:base:param:listPage";
    public static final String DETAIL = "sys:base:param:detail";
    public static final String SAVE = "sys:base:param:save";
    public static final String DELETE = "sys:base:param:delete";

    private SysParamPermission() {
    }
}
