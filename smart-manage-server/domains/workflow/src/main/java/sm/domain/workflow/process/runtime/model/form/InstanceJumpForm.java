package sm.domain.workflow.process.runtime.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InstanceJumpForm(@NotNull Long instanceId, @NotNull Long taskId,
                               @NotBlank @Size(max = 100) String targetNodeCode,
                               @NotBlank @Pattern(regexp = "RETURN|JUMP") String action,
                               @NotBlank @Size(max = 500) String reason,
                               @NotNull UUID requestId) { }
