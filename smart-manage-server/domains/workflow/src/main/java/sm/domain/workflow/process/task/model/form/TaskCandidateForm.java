package sm.domain.workflow.process.task.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
@Schema(description = "替换当前任务候选人请求")
public record TaskCandidateForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "引擎任务 ID") @NotNull Long taskId,
        @Schema(description = "目标候选人用户 ID 列表") @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> candidateIds,
        @Schema(description = "操作原因") @NotBlank @Size(max = 1000) String reason) { }
