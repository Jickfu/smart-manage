package sm.domain.workflow.process.runtime.model.form;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record WithdrawForm(@NotNull Long id, @NotNull UUID requestId) { }
