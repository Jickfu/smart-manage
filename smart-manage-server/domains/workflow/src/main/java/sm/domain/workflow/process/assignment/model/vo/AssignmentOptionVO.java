package sm.domain.workflow.process.assignment.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "审批人候选选项")
public record AssignmentOptionVO(
        @Schema(description = "流程定义持久化的审批人规则标识") String storageId,
        @Schema(description = "候选编码或编码搜索条件") String handlerCode,
        @Schema(description = "候选显示名称或名称搜索条件") String handlerName) { }
