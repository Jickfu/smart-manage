package sm.domain.demo.office.leave.service;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.numberrule.contract.NumberReferenceProvider;
import sm.domain.sys.base.numberrule.contract.model.*;
import java.util.List;
import java.util.Set;
@Component
public class LeaveNumberReferenceProvider implements NumberReferenceProvider {
    static final String REFERENCE = "demo/office/leave.number";
    @Override public NumberReferenceDefinition definition() {
        return new NumberReferenceDefinition(REFERENCE, "demo/office/leave", Set.of(NumberScopeType.GLOBAL, NumberScopeType.ORG),
                List.of(new NumberVariableDefinition("org.number", "组织编码", NumberSegmentType.VARIABLE),
                        new NumberVariableDefinition("bill.bizDate", "业务日期", NumberSegmentType.DATE)));
    }
}
