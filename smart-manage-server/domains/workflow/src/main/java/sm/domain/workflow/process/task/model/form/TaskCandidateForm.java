package sm.domain.workflow.process.task.model.form;
import jakarta.validation.constraints.*;
import java.util.List;
public record TaskCandidateForm(@NotNull Long instanceId, @NotNull Long taskId,
                                @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> candidateIds,
                                @NotBlank @Size(max = 1000) String reason) { }
