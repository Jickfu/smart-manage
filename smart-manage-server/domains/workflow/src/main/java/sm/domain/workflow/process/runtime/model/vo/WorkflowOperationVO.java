package sm.domain.workflow.process.runtime.model.vo;

import java.time.LocalDateTime;

public record WorkflowOperationVO(Long id, Long taskId, String action, Long operatorId,
                                  String reason, LocalDateTime time) { }
