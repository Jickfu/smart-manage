package sm.domain.workflow.process.task.model.vo;
import sm.domain.workflow.process.engine.WorkflowEngine;
public record TaskListVO(Long id, String businessType, Long businessId, String number,
                         WorkflowEngine.State state, String currentNode) { }
