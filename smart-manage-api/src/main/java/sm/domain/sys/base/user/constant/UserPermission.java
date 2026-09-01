package sm.domain.sys.base.user.constant;

/** 用户管理权限码。 */
public final class UserPermission {
    public static final String LIST = "sys:base:user:listPage";
    public static final String DETAIL = "sys:base:user:detail";
    public static final String SAVE = "sys:base:user:save";
    public static final String DELETE = "sys:base:user:delete";
    public static final String ENABLE = "sys:base:user:enable";
    public static final String DISABLE = "sys:base:user:disable";
    public static final String ASSIGN_ROLES = "sys:base:user:assignRoles";
    public static final String RESET_PASSWORD = "sys:base:user:resetPassword";
    public static final String TEMPORARY_LOGIN = "sys:base:user:temporaryLogin";
    public static final String READ_SENSITIVE = "sys:base:user:sensitive:read";
    public static final String IMPORT = "sys:base:user:import";
    public static final String EXPORT = "sys:base:user:export";

    private UserPermission() {
    }
}
