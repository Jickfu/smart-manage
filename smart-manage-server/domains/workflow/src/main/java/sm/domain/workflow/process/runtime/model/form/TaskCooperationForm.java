package sm.domain.workflow.process.runtime.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record TaskCooperationForm(@NotNull Long instanceId, @NotNull Long taskId,
                                  @NotBlank @Pattern(regexp = "TRANSFER|DELEGATE|ADD_SIGN|REDUCE_SIGN") String action,
                                  @NotEmpty @Size(max = 100) List<@NotNull Long> targetUserIds,
                                  @NotBlank @Size(max = 500) String reason,
                                  @NotNull UUID requestId) { }
