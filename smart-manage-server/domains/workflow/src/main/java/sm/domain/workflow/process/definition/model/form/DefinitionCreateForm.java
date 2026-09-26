package sm.domain.workflow.process.definition.model.form;
import jakarta.validation.constraints.*;
public record DefinitionCreateForm(@NotBlank @Pattern(regexp = "[a-z][a-z0-9_-]{0,39}") String number,
                                   @NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 100) String businessType) { }
