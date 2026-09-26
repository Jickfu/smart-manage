package sm.domain.demo.office.leave.model.form;
import jakarta.validation.constraints.NotNull;
public record LeaveApprovalDetailForm(@NotNull Long id, @NotNull Long instanceId) { }
