package sm.domain.workflow.process.task.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "活动任务和候选人的查询投影")
public class TaskMonitorRow {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "工作流实例 ID")
    private Long instanceId;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "可审批业务的稳定类型标识")
    private String businessType;
    @Schema(description = "流程定义内的节点编码")
    private String nodeCode;
    @Schema(description = "节点显示名称")
    private String nodeName;
    @Schema(description = "实例是否处于活动状态")
    private Boolean active;
    @Schema(description = "逗号分隔的候选人用户 ID")
    private String candidateIds;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
