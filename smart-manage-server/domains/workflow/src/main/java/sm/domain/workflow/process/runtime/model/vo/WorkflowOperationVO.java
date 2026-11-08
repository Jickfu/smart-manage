package sm.domain.workflow.process.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "流程管理操作记录")
public record WorkflowOperationVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "引擎任务 ID") Long taskId,
        @Schema(description = "工作流动作编码") String action,
        @Schema(description = "操作人用户 ID") Long operatorId,
        @Schema(description = "操作原因") String reason,
        @Schema(description = "事件发生时间") LocalDateTime time) { }
