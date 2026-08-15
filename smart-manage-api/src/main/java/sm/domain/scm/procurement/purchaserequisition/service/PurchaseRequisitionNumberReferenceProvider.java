package sm.domain.scm.procurement.purchaserequisition.service;

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
public class PurchaseRequisitionNumberReferenceProvider implements NumberReferenceProvider {
    @Override
    public NumberReferenceDefinition definition() {
        return new NumberReferenceDefinition(
                NumberRuleKeys.PURCHASE_REQUISITION_REFERENCE,
                "scm/procurement/purchase-requisition",
                Set.of(NumberScopeType.GLOBAL, NumberScopeType.ORG),
                List.of(
                        new NumberVariableDefinition("org.number", "组织编码", NumberSegmentType.VARIABLE),
                        new NumberVariableDefinition("bill.bizDate", "业务日期", NumberSegmentType.DATE)
                ));
    }
}
