package sm.domain.workflow.process.log.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "流程流转事件查询结果")
public class FlowLogRow {
    @Schema(description = "带事件来源前缀的日志唯一标识")
    private String id;
    @Schema(description = "工作流实例 ID")
    private Long instanceId;
    @Schema(description = "引擎任务 ID")
    private Long taskId;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "可审批业务的稳定类型标识")
    private String businessType;
    @Schema(description = "节点显示名称")
    private String nodeName;
    @Schema(description = "工作流动作编码")
    private String action;
    @Schema(description = "操作人用户 ID")
    private Long operatorId;
    @Schema(description = "操作人显示名称")
    private String operatorName;
    @Schema(description = "管理操作原因或脚本失败说明，审批意见在此查询中为空")
    private String opinion;
    @Schema(description = "流转事件的绝对时间（ISO 8601）")
    private Instant eventTime;
    @Schema(description = "事件来源，取值 ENGINE、MANAGEMENT 或 SCRIPT")
    private String source;
}
