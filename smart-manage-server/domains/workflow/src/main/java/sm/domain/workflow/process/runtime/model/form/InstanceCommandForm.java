package sm.domain.workflow.process.runtime.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InstanceCommandForm(@NotNull Long instanceId,
                                  @NotBlank @Size(max = 500) String reason,
                                  @NotNull UUID requestId) { }
