package sm.domain.sys.base.role.constant;

/** 角色管理权限码。 */
public final class RolePermission {
    public static final String LIST = "sys:base:role:listPage";
    public static final String SELECT = "sys:base:role:select";
    public static final String DETAIL = "sys:base:role:detail";
    public static final String SAVE = "sys:base:role:save";
    public static final String DELETE = "sys:base:role:delete";
    public static final String ASSIGN_PERMISSIONS = "sys:base:role:assignPermissions";
    public static final String ASSIGN_DATA_SCOPES = "sys:base:role:assignDataScopes";

    private RolePermission() {
    }
}

