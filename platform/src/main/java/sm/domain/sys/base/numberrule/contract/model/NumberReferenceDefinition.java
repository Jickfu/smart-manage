package sm.domain.sys.base.numberrule.contract.model;

import java.util.List;
import java.util.Set;

/** 业务模块通过代码注册的编号能力边界，不暴露表名和任意属性路径。 */
public record NumberReferenceDefinition(
        String referenceKey,
        String featureKey,
        Set<NumberScopeType> allowedScopes,
        List<NumberVariableDefinition> variables
) {
}
