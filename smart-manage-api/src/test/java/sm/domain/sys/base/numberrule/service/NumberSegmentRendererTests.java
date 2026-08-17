package sm.domain.sys.base.numberrule.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.numberrule.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.NumberScopeType;
import sm.domain.sys.base.numberrule.model.NumberSegmentType;
import sm.domain.sys.base.numberrule.model.NumberVariableDefinition;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.system.exception.BizException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberSegmentRendererTests {
    private final NumberReferenceDefinition reference = new NumberReferenceDefinition(
            "purchase", "feature", Set.of(NumberScopeType.ORG),
            List.of(
                    new NumberVariableDefinition("org.number", "组织编码", NumberSegmentType.VARIABLE),
                    new NumberVariableDefinition("bill.bizDate", "业务日期", NumberSegmentType.DATE)));

    @Test
    void rendersControlledSegments() {
        List<NumberRuleSegmentEntity> segments = List.of(
                segment(1, "VARIABLE", "org.number", null, null, "-"),
                segment(2, "FIXED", "PR", null, null, "-"),
                segment(3, "DATE", "bill.bizDate", "yyyyMMdd", null, "-"),
                segment(4, "SEQUENCE", null, null, 5, ""));
        NumberSegmentRenderer.validate(segments, reference);
        NumberVariableResolverRegistry resolvers = new NumberVariableResolverRegistry(List.of(
                new NumberVariableResolver() {
                    public String variableKey() { return "org.number"; }
                    public String resolve(NumberGenerationContext context) { return "SM"; }
                }));

        assertEquals("SM-PR-20260815-00012", NumberSegmentRenderer.render(segments, 12,
                NumberGenerationContext.forOrganization(1L, LocalDate.of(2026, 8, 15)), resolvers));
    }

    @Test
    void rejectsUnknownVariablesAndMultipleSequences() {
        assertThrows(BizException.class, () -> NumberSegmentRenderer.validate(List.of(
                segment(1, "VARIABLE", "entity.field", null, null, "-"),
                segment(2, "SEQUENCE", null, null, 5, "")), reference));
        assertThrows(BizException.class, () -> NumberSegmentRenderer.validate(List.of(
                segment(1, "SEQUENCE", null, null, 4, "-"),
                segment(2, "SEQUENCE", null, null, 5, "")), reference));
    }

    @Test
    void rendersBuiltInSystemDateWithoutBusinessDate() {
        List<NumberRuleSegmentEntity> segments = List.of(
                segment(1, "DATE", NumberRuleBuiltInVariables.SYSTEM_DATE_KEY, "yyyyMMdd", null, "-"),
                segment(2, "SEQUENCE", null, null, 3, ""));

        NumberSegmentRenderer.validate(segments, reference);

        assertEquals(LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "-001",
                NumberSegmentRenderer.render(segments, 1,
                        new NumberGenerationContext(null, null, null),
                        new NumberVariableResolverRegistry(List.of())));
    }

    private NumberRuleSegmentEntity segment(int sort, String type, String value, String format,
                                            Integer length, String separator) {
        NumberRuleSegmentEntity segment = new NumberRuleSegmentEntity();
        segment.setSort(sort);
        segment.setSegmentType(type);
        segment.setValue(value);
        segment.setFormat(format);
        segment.setLength(length);
        segment.setSeparator(separator);
        return segment;
    }
}
