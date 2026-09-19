package sm.domain.sys.base.attachmentconfig.constant;

/** 附件配置权限码，统一供接口鉴权和权限审计使用。 */
public final class AttachmentConfigPermission {
    public static final String DETAIL = "sys:base:attachment-config:detail";
    public static final String SAVE = "sys:base:attachment-config:save";

    private AttachmentConfigPermission() {
    }
}
