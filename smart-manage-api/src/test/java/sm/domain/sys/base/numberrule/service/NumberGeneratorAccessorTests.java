package sm.domain.sys.base.numberrule.service;

import sm.domain.sys.base.numberrule.contract.NumberReferenceProvider;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.numberrule.mapper.NumberReferenceMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleSegmentMapper;
import sm.domain.sys.base.numberrule.contract.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.contract.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.contract.model.NumberScopeType;
import sm.domain.sys.base.numberrule.contract.model.NumberSegmentType;
import sm.domain.sys.base.numberrule.contract.model.NumberVariableDefinition;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.system.exception.BizException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NumberGeneratorAccessorTests {
    @Test
    void requiresAnActiveBusinessTransaction() {
        NumberGeneratorAccessor accessor = accessor(mock(NumberRuleMapper.class),
                mock(NumberRuleSegmentMapper.class));
        assertThrows(BizException.class, () -> accessor.nextNumber("reference", "rule",
                NumberGenerationContext.forCategory(1L)));
    }

    @Test
    void advancesOrganizationDailySegmentAtomically() {
        NumberRuleMapper mapper = mock(NumberRuleMapper.class);
        NumberRuleSegmentMapper segmentMapper = mock(NumberRuleSegmentMapper.class);
        NumberRuleEntity rule = new NumberRuleEntity();
        rule.setRuleKey("purchase");
        rule.setReferenceKey("reference");
        rule.setScopeType("ORG");
        rule.setResetPeriod("DAY");
        rule.setStartValue(1L);
        rule.setEnabled(true);
        when(mapper.selectOne(any())).thenReturn(rule);
        when(mapper.nextValue("purchase", "20", "20260815", 1L)).thenReturn(7L);
        NumberRuleSegmentEntity sequence = new NumberRuleSegmentEntity();
        sequence.setSort(1);
        sequence.setSegmentType("SEQUENCE");
        sequence.setLength(5);
        sequence.setSeparator("");
        when(segmentMapper.selectList(any())).thenReturn(List.of(sequence));
        NumberGeneratorAccessor accessor = accessor(mapper, segmentMapper);

        try (MockedStatic<TransactionSynchronizationManager> transaction =
                     mockStatic(TransactionSynchronizationManager.class)) {
            transaction.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);
            String number = accessor.nextNumber("reference", "purchase",
                    NumberGenerationContext.forOrganization(20L, LocalDate.of(2026, 8, 15)));
            assertEquals("00007", number);
            verify(mapper).nextValue("purchase", "20", "20260815", 1L);
        }
    }

    @Test
    void rendersSystemDateWithoutBusinessDate() {
        NumberRuleMapper mapper = mock(NumberRuleMapper.class);
        NumberRuleSegmentMapper segmentMapper = mock(NumberRuleSegmentMapper.class);
        NumberRuleEntity rule = new NumberRuleEntity();
        rule.setRuleKey("system-date");
        rule.setReferenceKey("reference");
        rule.setScopeType("ORG");
        rule.setResetPeriod("NEVER");
        rule.setStartValue(1L);
        rule.setEnabled(true);
        when(mapper.selectOne(any())).thenReturn(rule);
        when(mapper.nextValue("system-date", "20", "NEVER", 1L)).thenReturn(1L);
        NumberRuleSegmentEntity date = new NumberRuleSegmentEntity();
        date.setSort(1);
        date.setSegmentType("DATE");
        date.setValue(NumberRuleBuiltInVariables.SYSTEM_DATE_KEY);
        date.setFormat("yyyyMMdd");
        date.setSeparator("-");
        NumberRuleSegmentEntity sequence = new NumberRuleSegmentEntity();
        sequence.setSort(2);
        sequence.setSegmentType("SEQUENCE");
        sequence.setLength(3);
        sequence.setSeparator("");
        when(segmentMapper.selectList(any())).thenReturn(List.of(date, sequence));
        NumberGeneratorAccessor accessor = accessor(mapper, segmentMapper);

        try (MockedStatic<TransactionSynchronizationManager> transaction =
                     mockStatic(TransactionSynchronizationManager.class)) {
            transaction.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);
            String number = accessor.nextNumber("reference", "system-date",
                    NumberGenerationContext.forOrganization(20L, null));
            assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "-001",
                    number);
        }
    }

    private NumberGeneratorAccessor accessor(NumberRuleMapper mapper, NumberRuleSegmentMapper segmentMapper) {
        NumberReferenceDefinition definition = new NumberReferenceDefinition(
                "reference", "feature", Set.of(NumberScopeType.ORG),
                List.of(new NumberVariableDefinition("org.number", "组织编码", NumberSegmentType.VARIABLE)));
        NumberReferenceProvider provider = () -> definition;
        return new NumberGeneratorAccessor(mapper, mock(NumberReferenceMapper.class), segmentMapper,
                new NumberReferenceRegistry(List.of(provider)), new NumberVariableResolverRegistry(List.of()));
    }
}
