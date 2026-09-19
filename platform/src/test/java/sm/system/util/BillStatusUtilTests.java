package sm.system.util;

import org.junit.jupiter.api.Test;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillStatusUtilTests {

    @Test
    void blankStatusUsesNullSafeSemantics() {
        assertEquals(BillStatusEnum.SAVED.getValue(), BillStatusUtil.defaultSaved(null));
        assertEquals(BillStatusEnum.SAVED.getValue(), BillStatusUtil.defaultSaved(""));
        assertEquals(BillStatusEnum.SAVED.getValue(), BillStatusUtil.defaultSaved(" \t"));
        // 保留 Hutool 比 JDK isBlank 更宽的历史空白语义。
        assertEquals(BillStatusEnum.SAVED.getValue(), BillStatusUtil.defaultSaved("\u00a0\ufeff"));
        assertEquals(BillStatusEnum.SUBMITTED.getValue(),
                BillStatusUtil.defaultSaved(BillStatusEnum.SUBMITTED.getValue()));
        assertDoesNotThrow(() -> BillStatusUtil.requireCanSave(null));
        assertDoesNotThrow(() -> BillStatusUtil.requireCanSave(" \t"));
        assertDoesNotThrow(() -> BillStatusUtil.requireCanSave("\u00a0\ufeff"));
    }

    @Test
    void savedBillCanBeSubmitted() {
        assertEquals(BillStatusEnum.SUBMITTED.getValue(),
                BillStatusUtil.submit(BillStatusEnum.SAVED.getValue()));
    }

    @Test
    void submittedBillCannotBeSavedAgain() {
        BizException exception = assertThrows(BizException.class,
                () -> BillStatusUtil.requireCanSave(BillStatusEnum.SUBMITTED.getValue()));
        assertEquals(ResultEnum.BILL_STATUS_ERROR.getCode(), exception.getCode());
    }
}
