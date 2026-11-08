package sm.domain.workflow.process.definition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "发布已保存流程版本请求")
public record DefinitionPublishForm(
        @Schema(description = "引擎流程定义版本 ID") @NotNull Long definitionId,
        @Schema(description = "已保存设计内容的 SHA-256 摘要") @NotBlank @Size(max = 64) String digest) { }
