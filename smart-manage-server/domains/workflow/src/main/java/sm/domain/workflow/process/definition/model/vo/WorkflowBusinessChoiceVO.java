package sm.domain.workflow.process.definition.model.vo;

/** 已装配审批能力与平台目录的交集，目录字段只作参照筛选。 */
public record WorkflowBusinessChoiceVO(String key, String name, String featureKey,
                                      Long domainId, String domainName, Long appId, String appName) { }
