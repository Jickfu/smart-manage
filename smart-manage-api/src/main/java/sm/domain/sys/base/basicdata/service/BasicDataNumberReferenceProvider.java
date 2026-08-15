package sm.domain.sys.base.basicdata.service;

import org.springframework.stereotype.Component;
import sm.domain.sys.base.numberrule.constant.NumberRuleKeys;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.NumberScopeType;
import sm.domain.sys.base.numberrule.model.NumberSegmentType;
import sm.domain.sys.base.numberrule.model.NumberVariableDefinition;
import sm.domain.sys.base.numberrule.service.NumberReferenceProvider;

import java.util.List;
import java.util.Set;

@Component
public class BasicDataNumberReferenceProvider implements NumberReferenceProvider {
    @Override
    public NumberReferenceDefinition definition() {
        return new NumberReferenceDefinition(
                NumberRuleKeys.BASIC_DATA_ITEM_REFERENCE,
                "sys/base/basic-data",
                Set.of(NumberScopeType.GLOBAL, NumberScopeType.CATEGORY),
                List.of(new NumberVariableDefinition(
                        "category.number", "基础资料分类编码", NumberSegmentType.VARIABLE)));
    }
}
