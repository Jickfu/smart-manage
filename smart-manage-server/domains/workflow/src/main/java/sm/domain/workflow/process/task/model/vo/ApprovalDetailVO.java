package sm.domain.workflow.process.task.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import sm.domain.workflow.process.engine.WorkflowEngine;
@Schema(description = "参与人可见的审批轮次详情")
public record ApprovalDetailVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "业务单据 ID") Long businessId,
        @Schema(description = "业务编码") String number,
        @Schema(description = "参与人可见的运行状态、任务和审批轨迹，变量字段为空") WorkflowEngine.Run run,
        @Schema(description = "当前申请人是否可撤回本轮审批") boolean canWithdraw,
        @Schema(description = "当前用户可处理的任务 ID，无可处理任务时为空") Long currentTaskId,
        @Schema(description = "当前处理人是否可拿回任务") boolean canTakeBack,
        @Schema(description = "人员名称映射，键为用户 ID") java.util.Map<Long, String> actorNames) { }
