package sm.domain.workflow.process.definition.model.vo;
import tools.jackson.databind.JsonNode;
public record DefinitionDesignVO(JsonNode definition, String digest) { }
