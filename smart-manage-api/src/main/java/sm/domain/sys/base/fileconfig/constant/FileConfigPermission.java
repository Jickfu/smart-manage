package sm.domain.sys.base.fileconfig.constant;

/** 文件存储配置权限码，统一供接口鉴权和权限审计使用。 */
public final class FileConfigPermission {
    public static final String DETAIL = "sys:base:file-config:detail";
    public static final String SAVE = "sys:base:file-config:save";

    private FileConfigPermission() {
    }
}
