package sm.domain.workflow.process.definition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "修改流程定义启停状态请求")
public record DefinitionEnabledForm(
        @Schema(description = "ID") @NotNull Long id,
        @Schema(description = "乐观锁版本，修改已有记录时使用") @NotNull Integer version,
        @Schema(description = "是否启用") @NotNull Boolean enabled) { }
