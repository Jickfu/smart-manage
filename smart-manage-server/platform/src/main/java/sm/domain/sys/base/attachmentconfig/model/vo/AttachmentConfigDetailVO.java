package sm.domain.sys.base.attachmentconfig.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "附件配置详情输出模型")
@Data
public class AttachmentConfigDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "单文件最大字节数")
    private Long maxUploadBytes;
    @Schema(description = "允许扩展名，逗号分隔")
    private List<String> allowedExtensions;
    @Schema(description = "允许MIME类型，逗号分隔")
    private List<String> allowedMimeTypes;
    @Schema(description = "临时附件有效小时数")
    private Integer tempExpireHours;
}
