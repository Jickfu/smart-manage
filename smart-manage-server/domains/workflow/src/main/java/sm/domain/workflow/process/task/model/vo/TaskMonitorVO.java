package sm.domain.workflow.process.task.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "活动审批任务监控行")
public record TaskMonitorVO(
        @Schema(description = "ID") Long id,
        @Schema(description = "工作流实例 ID") Long instanceId,
        @Schema(description = "业务编码") String number,
        @Schema(description = "可审批业务的稳定类型标识") String businessType,
        @Schema(description = "流程定义内的节点编码") String nodeCode,
        @Schema(description = "节点显示名称") String nodeName,
        @Schema(description = "实例是否处于活动状态") boolean active,
        @Schema(description = "当前候选人的用户 ID 列表") List<Long> candidates,
        @Schema(description = "候选人名称映射，键为用户 ID") Map<Long, String> candidateNames,
        @Schema(description = "创建时间") LocalDateTime createTime) { }
