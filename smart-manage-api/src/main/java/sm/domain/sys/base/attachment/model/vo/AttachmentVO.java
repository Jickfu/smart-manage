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
    /** 存储实现返回的访问地址；浏览器是否可直接访问由具体业务入口决定。 */
    private String url;
    private String createTime;
}
