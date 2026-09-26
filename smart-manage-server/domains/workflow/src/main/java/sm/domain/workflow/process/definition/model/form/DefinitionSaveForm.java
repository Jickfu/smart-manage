package sm.domain.workflow.process.definition.model.form;
import jakarta.validation.constraints.*;
import tools.jackson.databind.JsonNode;
public record DefinitionSaveForm(@NotNull Long definitionId, @NotNull JsonNode definition,
                                 @NotBlank @Size(max = 64) String digest) { }
