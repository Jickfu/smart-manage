package sm.domain.workflow.process.definition.model.form;
import jakarta.validation.constraints.*;
public record DefinitionEnabledForm(@NotNull Long id, @NotNull Integer version, @NotNull Boolean enabled) { }
