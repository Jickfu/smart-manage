package sm.domain.sys.base.attachment.model.vo;

import lombok.Data;

/**
 * 附件 VO
 *
 * @author Chekfu
 */
@Data
public class AttachmentVO {

    private Long id;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private String fileExt;
    private Boolean isTemp;
    private String uploadSessionId;
    /** 公开访问 URL */
    private String url;
    private String createTime;
}
