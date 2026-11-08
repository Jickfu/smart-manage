package sm.domain.workflow.process.definition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/** 已装配审批能力与平台目录的交集，目录字段只作参照筛选。 */
@Schema(description = "可绑定审批的业务选项")
public record WorkflowBusinessChoiceVO(
        @Schema(description = "可审批业务的注册标识") String key,
        @Schema(description = "显示名称") String name,
        @Schema(description = "所属功能目录键") String featureKey,
        @Schema(description = "所属领域 ID") Long domainId,
        @Schema(description = "所属领域名称") String domainName,
        @Schema(description = "所属应用 ID") Long appId,
        @Schema(description = "所属应用名称") String appName) { }
