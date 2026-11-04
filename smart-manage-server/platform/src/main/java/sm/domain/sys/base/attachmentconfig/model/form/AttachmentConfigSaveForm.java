package sm.domain.sys.base.attachmentconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "附件配置保存输入表单")
@Data
public class AttachmentConfigSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "单文件最大字节数")
    @NotNull
    @Min(1)
    @Max(value = 104857600, message = "单文件最大大小不能超过服务端 100MB 上限")
    private Long maxUploadBytes;
    @Schema(description = "允许扩展名，逗号分隔")
    @NotEmpty
    private List<String> allowedExtensions;
    @Schema(description = "允许MIME类型，逗号分隔")
    @NotEmpty
    private List<String> allowedMimeTypes;
    @Schema(description = "临时附件有效小时数")
    @NotNull
    @Min(1)
    @Max(168)
    private Integer tempExpireHours;
}
