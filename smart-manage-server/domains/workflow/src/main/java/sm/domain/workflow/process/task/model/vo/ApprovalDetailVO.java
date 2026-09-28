package sm.domain.workflow.process.task.model.vo;
import sm.domain.workflow.process.engine.WorkflowEngine;
public record ApprovalDetailVO(Long id, String businessType, Long businessId, String number,
                               WorkflowEngine.Run run, boolean canWithdraw, Long currentTaskId,
                               boolean canTakeBack,
                               java.util.Map<Long, String> actorNames) { }
