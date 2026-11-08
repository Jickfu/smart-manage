package sm.domain.workflow.process.definition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;
@Schema(description = "流程版本的设计内容和摘要")
public record DefinitionDesignVO(
        @Schema(description = "引擎流程图设计 JSON") JsonNode definition,
        @Schema(description = "已保存设计内容的 SHA-256 摘要") String digest) { }
