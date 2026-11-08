package sm.domain.workflow.process.definition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "新增流程定义请求")
public record DefinitionCreateForm(
        @Schema(description = "业务编码") @NotBlank @Pattern(regexp = "[a-z][a-z0-9_-]{0,39}") String number,
        @Schema(description = "显示名称") @NotBlank @Size(max = 100) String name,
        @Schema(description = "可审批业务的稳定类型标识") @NotBlank @Size(max = 100) String businessType) { }
