package sm.domain.sys.base.attachment.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 已完成对象权限校验的附件下载方式。 */
@Data
@AllArgsConstructor
public class AttachmentDownloadAccessVO {
    /** S3 等私有对象存储的短时直连地址；为空时由后端下载接口代理。 */
    private String directUrl;
}
