package sm.domain.workflow.process.task.model.vo;
import java.time.LocalDateTime;
import java.util.List;
public record CandidateChangeVO(Long id, Long taskId, Long operatorId, List<Long> beforeCandidates,
                                List<Long> afterCandidates, String reason, LocalDateTime time) { }
