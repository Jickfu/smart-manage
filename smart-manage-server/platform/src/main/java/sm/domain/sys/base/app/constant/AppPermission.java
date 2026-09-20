package sm.domain.sys.base.app.constant;

/** 应用管理权限码，统一供接口鉴权和权限审计使用。 */
public final class AppPermission {
    public static final String LIST = "sys:base:app:listPage";
    public static final String DETAIL = "sys:base:app:detail";
    public static final String SAVE = "sys:base:app:save";
    public static final String DELETE = "sys:base:app:delete";
    public static final String ENABLE = "sys:base:app:enable";
    public static final String DISABLE = "sys:base:app:disable";

    private AppPermission() {
    }
}

