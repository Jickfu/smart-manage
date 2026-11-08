package sm.domain.workflow.process.instance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import sm.domain.workflow.process.engine.WorkflowEngine;

import java.time.LocalDateTime;

@Schema(description = "流程实例列表行")
public record InstanceListVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "业务单据 ID") Long businessId,
        @Schema(description = "业务编码") String number,
        @Schema(description = "单据所属组织 ID") Long orgId,
        @Schema(description = "申请人用户 ID") Long applicantId,
        @Schema(description = "申请人显示名称") String applicantName,
        @Schema(description = "流程运行状态，取值见工作流状态枚举") WorkflowEngine.State state,
        @Schema(description = "实例是否处于活动状态") boolean active,
        @Schema(description = "当前活动任务的节点名称") String currentNode,
        @Schema(description = "创建时间") LocalDateTime createTime) { }
