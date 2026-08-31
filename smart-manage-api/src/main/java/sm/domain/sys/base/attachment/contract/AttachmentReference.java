package sm.domain.sys.base.attachment.contract;

import lombok.Data;

/** 附件领域向业务领域发布的只读引用信息。 */
@Data
public class AttachmentReference {

    private Long id;
    private Long businessAttachmentId;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private String fileExt;
    private Boolean isTemp;
    private String uploadSessionId;
    private String createTime;
    private Long uploaderId;
    private String uploaderName;
    private String remark;
}
