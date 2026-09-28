package sm.domain.workflow.process.runtime.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record VariableUpdateForm(@NotNull Long instanceId,
                                 @NotNull Map<String, Object> changes,
                                 @NotNull List<String> removals,
                                 @NotBlank @Size(max = 500) String reason,
                                 @NotNull UUID requestId) { }
