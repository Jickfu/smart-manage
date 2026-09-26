package sm.domain.workflow.process.runtime.model.form;
import jakarta.validation.constraints.*;
public record ApprovalForm(@NotNull Long instanceId, @NotNull Long taskId,
                           @NotBlank @Pattern(regexp = "APPROVE|REJECT") String action,
                           @Size(max = 1000) String opinion, @NotNull java.util.UUID requestId) { }
