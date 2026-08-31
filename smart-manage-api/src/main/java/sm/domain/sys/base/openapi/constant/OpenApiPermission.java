package sm.domain.sys.base.openapi.constant;

/** OpenAPI 平台管理端权限。 */
public final class OpenApiPermission {
    public static final String APPLICATION_LIST = "sys:base:openapi-application:listPage";
    public static final String APPLICATION_DETAIL = "sys:base:openapi-application:detail";
    public static final String APPLICATION_SAVE = "sys:base:openapi-application:save";
    public static final String APPLICATION_ENABLE = "sys:base:openapi-application:enable";
    public static final String APPLICATION_CREDENTIAL = "sys:base:openapi-application:credential";
    public static final String APPLICATION_GRANT = "sys:base:openapi-application:grant";
    public static final String CATALOG_LIST = "sys:base:openapi-catalog:listPage";
    public static final String CATALOG_PUBLISH = "sys:base:openapi-catalog:publish";
    public static final String INVOCATION_LIST = "sys:base:openapi-invocation:listPage";

    private OpenApiPermission() {
    }
}
