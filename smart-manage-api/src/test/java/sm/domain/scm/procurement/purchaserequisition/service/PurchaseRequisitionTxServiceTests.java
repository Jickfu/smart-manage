package sm.domain.scm.procurement.purchaserequisition.service;

import org.junit.jupiter.api.Test;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionEntryMapper;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionMapper;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurchaseRequisitionTxServiceTests {

    @Test
    void deleteRejectsStaleVersionBeforeWriting() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mapper, mock(PurchaseRequisitionEntryMapper.class));

        BizException exception = assertThrows(BizException.class, () -> service.deleteById(1L, 1));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void deleteReportsConflictWhenAtomicConditionNoLongerMatches() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.delete(any())).thenReturn(0);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mapper, mock(PurchaseRequisitionEntryMapper.class));

        BizException exception = assertThrows(BizException.class, () -> service.deleteById(1L, 2));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void submitRejectsStaleVersionBeforeWriting() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mapper, mock(PurchaseRequisitionEntryMapper.class));

        BizException exception = assertThrows(BizException.class, () -> service.submit(1L, 1));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void submitReportsConflictWhenAtomicConditionNoLongerMatches() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.update(any(PurchaseRequisitionEntity.class), any())).thenReturn(0);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mapper, mock(PurchaseRequisitionEntryMapper.class));

        BizException exception = assertThrows(BizException.class, () -> service.submit(1L, 2));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }
}
