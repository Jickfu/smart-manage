package sm.domain.sys.base.numberrule.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.numberrule.mapper.NumberReferenceMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleSegmentMapper;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NumberRuleTxServiceTests {
    @Test
    void disablingAnActivelyReferencedRuleIsRejected() {
        NumberRuleMapper mapper = mock(NumberRuleMapper.class);
        NumberRuleEntity rule = rule(true);
        when(mapper.selectBatchIds(List.of(1L))).thenReturn(List.of(rule));
        when(mapper.countDefaultReferences("rule")).thenReturn(1L);
        NumberRuleTxService service = service(mapper);

        assertThrows(BizException.class, () -> service.updateEnabled(List.of(1L), false));
    }

    @Test
    void disabledRuleCannotBecomeReferenceDefault() {
        NumberRuleMapper mapper = mock(NumberRuleMapper.class);
        when(mapper.selectById(1L)).thenReturn(rule(false));

        assertThrows(BizException.class, () -> service(mapper).setDefault(1L));
    }

    private NumberRuleTxService service(NumberRuleMapper mapper) {
        return new NumberRuleTxService(mapper, mock(NumberReferenceMapper.class),
                mock(NumberRuleSegmentMapper.class), new NumberReferenceRegistry(List.of()));
    }

    private NumberRuleEntity rule(boolean enabled) {
        NumberRuleEntity rule = new NumberRuleEntity();
        rule.setId(1L);
        rule.setRuleKey("rule");
        rule.setName("规则");
        rule.setEnabled(enabled);
        return rule;
    }
}
