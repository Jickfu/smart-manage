package sm.domain.workflow.process.instance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.vo.WorkflowOperationVO;
import sm.domain.workflow.process.task.model.vo.CandidateChangeVO;

import java.util.List;
import java.util.Map;

@Schema(description = "流程实例管理详情")
public record InstanceAdminDetailVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "业务单据 ID") Long businessId,
        @Schema(description = "业务编码") String number,
        @Schema(description = "单据所属组织 ID") Long orgId,
        @Schema(description = "申请人用户 ID") Long applicantId,
        @Schema(description = "管理端可见的运行状态、任务、流转轨迹及脱敏变量") WorkflowEngine.Run run,
        @Schema(description = "人员名称映射，键为用户 ID") Map<Long, String> actorNames,
        @Schema(description = "当前轮次的候选人维护记录列表") List<CandidateChangeVO> candidateChanges,
        @Schema(description = "当前轮次的管理操作记录列表") List<WorkflowOperationVO> operations) { }
