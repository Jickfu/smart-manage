package sm.domain.workflow.process.task.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import sm.domain.workflow.process.engine.WorkflowEngine;
@Schema(description = "个人任务列表行")
public record TaskListVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "业务单据 ID") Long businessId,
        @Schema(description = "业务编码") String number,
        @Schema(description = "流程运行状态，取值见工作流状态枚举") WorkflowEngine.State state,
        @Schema(description = "当前活动任务的节点名称") String currentNode) { }
