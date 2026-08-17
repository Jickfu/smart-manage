package sm.domain.sys.base.numberrule.service;

import sm.domain.sys.base.numberrule.model.NumberSegmentType;
import sm.domain.sys.base.numberrule.model.NumberVariableDefinition;

/** 编号规则内置变量，不依赖具体业务模块的编号引用注册。 */
final class NumberRuleBuiltInVariables {
    static final String SYSTEM_DATE_KEY = "system.date";
    static final NumberVariableDefinition SYSTEM_DATE =
            new NumberVariableDefinition(SYSTEM_DATE_KEY, "系统日期", NumberSegmentType.DATE);

    private NumberRuleBuiltInVariables() {
    }
}
