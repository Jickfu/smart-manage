package sm.domain.sys.base.attachment.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 已完成对象权限校验的附件下载方式。
 */
@Schema(description = "附件下载访问信息输出模型")
@Data
@AllArgsConstructor
public class AttachmentDownloadAccessVO {
    /**
     * S3 等私有对象存储的短时直连地址；为空时由后端下载接口代理
     */
    @Schema(description = "可直接访问的下载 URL，可能包含临时签名")
    private String directUrl;
}
