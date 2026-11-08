package sm.domain.workflow.process.task.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
@Schema(description = "任务候选人维护记录")
public record CandidateChangeVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "引擎任务 ID") Long taskId,
        @Schema(description = "操作人用户 ID") Long operatorId,
        @Schema(description = "变更前的候选人列表") List<Long> beforeCandidates,
        @Schema(description = "变更后的候选人列表") List<Long> afterCandidates,
        @Schema(description = "操作原因") String reason,
        @Schema(description = "事件发生时间") LocalDateTime time) { }
