package sm.domain.sys.base.feature.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "功能保存输入表单")
@Data
public class FeatureSaveForm {
    @Schema(description = "ID")
    @NotNull
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull
    private Integer version;
    @Schema(description = "管理员自定义名称")
    private String customName;
    @Schema(description = "管理员自定义排序")
    private Integer customSeq;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "是否在功能目录中可见")
    @NotNull
    private Boolean visible;
}
