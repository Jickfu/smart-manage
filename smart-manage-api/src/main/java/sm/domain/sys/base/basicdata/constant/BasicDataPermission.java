package sm.domain.sys.base.basicdata.constant;

/** 基础数据权限码，统一供接口鉴权和权限审计使用。 */
public final class BasicDataPermission {
    public static final String LIST = "sys:base:basic-data:listPage";
    public static final String DETAIL = "sys:base:basic-data:detail";
    public static final String SAVE = "sys:base:basic-data:save";
    public static final String DELETE = "sys:base:basic-data:delete";
    public static final String ENABLE = "sys:base:basic-data:enable";
    public static final String DISABLE = "sys:base:basic-data:disable";

    private BasicDataPermission() {
    }
}
