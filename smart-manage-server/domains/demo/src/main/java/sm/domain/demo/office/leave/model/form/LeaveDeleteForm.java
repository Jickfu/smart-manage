package sm.domain.demo.office.leave.model.form;
import jakarta.validation.constraints.NotNull;
public record LeaveDeleteForm(@NotNull Long id, @NotNull Integer version) { }
