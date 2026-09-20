package sm.domain.sys.base.datascope.model;

import java.util.List;

/** 角色数据范围规则的模块边界快照。 */
public record DataScopeRuleSnapshot(String resourceType, String action, String scopeType,
                                    List<Long> orgIds) {
}
