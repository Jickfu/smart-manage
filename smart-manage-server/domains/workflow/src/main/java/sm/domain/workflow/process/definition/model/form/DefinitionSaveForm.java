package sm.domain.workflow.process.definition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import tools.jackson.databind.JsonNode;
@Schema(description = "保存流程设计草稿请求")
public record DefinitionSaveForm(
        @Schema(description = "引擎流程定义版本 ID") @NotNull Long definitionId,
        @Schema(description = "引擎流程图设计 JSON") @NotNull JsonNode definition,
        @Schema(description = "已保存设计内容的 SHA-256 摘要") @NotBlank @Size(max = 64) String digest) { }
