package sm.system.bill;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillStatusPolicyTests {

    @Test
    void standardPolicyUsesStableStatusContract() {
        BillStatusPolicy policy = BillStatusPolicy.standard();

        assertEquals("A", policy.initialStatus());
        assertEquals(List.of("A", "B", "C", "D"),
                policy.definitions().stream().map(BillStatusDefinition::value).toList());
        assertEquals("已审核", policy.requireKnown("C").name());
        assertTrue(policy.isEditable("A"));
        assertFalse(policy.isEditable("B"));
        assertEquals("B", policy.transition(StandardBillStatusAction.SUBMIT, "A"));
        assertEquals("C", policy.transition(StandardBillStatusAction.AUDIT, "B"));
    }

    @Test
    void businessCanExtendWithoutOverridingStandardStatuses() {
        BillStatusPolicy policy = BillStatusPolicy.standardBuilder()
                .addStatus("E", "已下达")
                .transition("ISSUE", "C", "E")
                .build();

        assertEquals("E", policy.transition("ISSUE", "C"));
        assertEquals("已下达", policy.requireKnown("E").name());
        assertThrows(IllegalArgumentException.class,
                () -> BillStatusPolicy.standardBuilder().addStatus("A", "业务覆盖").build());
    }

    @Test
    void invalidOrUnavailableStatusesAreRejectedExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> new BillStatusDefinition("AA", "非法"));
        BizException unknown = assertThrows(BizException.class,
                () -> BillStatusPolicy.standard().requireKnown("E"));
        assertEquals(ResultEnum.BILL_STATUS_ERROR.getCode(), unknown.getCode());
        BizException invalidTransition = assertThrows(BizException.class,
                () -> BillStatusPolicy.standard().transition(StandardBillStatusAction.SUBMIT, "B"));
        assertEquals(ResultEnum.BILL_STATUS_ERROR.getCode(), invalidTransition.getCode());
    }
}
