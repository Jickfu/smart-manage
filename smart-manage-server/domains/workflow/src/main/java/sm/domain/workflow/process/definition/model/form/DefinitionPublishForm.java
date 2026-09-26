package sm.domain.workflow.process.definition.model.form;
import jakarta.validation.constraints.*;
public record DefinitionPublishForm(@NotNull Long definitionId, @NotBlank @Size(max = 64) String digest) { }
