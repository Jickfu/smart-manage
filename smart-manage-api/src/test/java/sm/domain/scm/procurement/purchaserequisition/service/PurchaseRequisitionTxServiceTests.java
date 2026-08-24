package sm.domain.scm.procurement.purchaserequisition.service;

import org.junit.jupiter.api.Test;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionEntryMapper;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionMapper;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntryEntity;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionEntryForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSaveForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSubmitForm;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.numberrule.service.NumberGeneratorAccessor;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class PurchaseRequisitionTxServiceTests {

    @Test
    void newBillCanBeSubmittedWithoutSavingFirst() throws IOException {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntryMapper entryMapper = mock(PurchaseRequisitionEntryMapper.class);
        when(mapper.insert(any(PurchaseRequisitionEntity.class))).thenAnswer(invocation -> {
            PurchaseRequisitionEntity inserted = invocation.getArgument(0);
            inserted.setId(1L);
            return 1;
        });
        when(entryMapper.insert(any(PurchaseRequisitionEntryEntity.class))).thenReturn(1);
        PurchaseRequisitionEntity savedEntity = new PurchaseRequisitionEntity();
        savedEntity.setId(1L);
        savedEntity.setVersion(0);
        savedEntity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(savedEntity);
        when(mapper.update(any(PurchaseRequisitionEntity.class), any())).thenReturn(1);

        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        when(currentUserContext.getOrgId()).thenReturn(1L);
        when(currentUserContext.getUserId()).thenReturn(1L);
        AttachmentService attachmentService = mock(AttachmentService.class);
        NumberGeneratorAccessor numberGeneratorAccessor = mock(NumberGeneratorAccessor.class);
        when(numberGeneratorAccessor.nextNumber(any(), any())).thenReturn("PR-20260728-00001");
        PurchaseRequisitionDataScope dataScope = mock(PurchaseRequisitionDataScope.class);
        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(currentUserContext, mapper, entryMapper,
                attachmentService, numberGeneratorAccessor, dataScope);
        PurchaseRequisitionSubmitForm form = submitForm(null, null);
        form.setAttachmentIds(List.of(99L));
        form.setAttachmentUploadSessions(Map.of(99L, "upload-session"));

        assertEquals(1L, service.submit(form));

        verify(mapper).insert(any(PurchaseRequisitionEntity.class));
        verify(entryMapper).insert(any(PurchaseRequisitionEntryEntity.class));
        verify(mapper).update(any(PurchaseRequisitionEntity.class), any());
        verify(attachmentService).promoteForAggregate(any());
        verify(dataScope, times(1)).requireAllowed(any(PurchaseRequisitionEntity.class),
                org.mockito.ArgumentMatchers.eq(PurchaseRequisitionResourceRegistration.ACTION_SUBMIT));
        verify(dataScope, never()).requireAllowed(any(PurchaseRequisitionEntity.class),
                org.mockito.ArgumentMatchers.eq(PurchaseRequisitionResourceRegistration.ACTION_DELETE));
    }

    @Test
    void newBillChecksSaveScopeBeforeInsert() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        when(currentUserContext.getOrgId()).thenReturn(10L);
        when(currentUserContext.getUserId()).thenReturn(20L);
        NumberGeneratorAccessor numberGeneratorAccessor = mock(NumberGeneratorAccessor.class);
        when(numberGeneratorAccessor.nextNumber(any(), any())).thenReturn("PR-001");
        PurchaseRequisitionDataScope dataScope = mock(PurchaseRequisitionDataScope.class);
        org.mockito.Mockito.doThrow(new BizException(ResultEnum.PERMISSION_ERROR, "无权访问该采购申请"))
                .when(dataScope).requireAllowed(any(PurchaseRequisitionEntity.class),
                        org.mockito.ArgumentMatchers.eq(PurchaseRequisitionResourceRegistration.ACTION_SAVE));
        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(currentUserContext, mapper,
                mock(PurchaseRequisitionEntryMapper.class), mock(AttachmentService.class), numberGeneratorAccessor, dataScope);

        assertThrows(BizException.class, () -> service.save(submitForm(null, null)));

        verify(mapper, never()).insert(any(PurchaseRequisitionEntity.class));
    }

    @Test
    void deleteRejectsStaleVersionBeforeWriting() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mock(CurrentUserContext.class), mapper, mock(PurchaseRequisitionEntryMapper.class),
                mock(AttachmentService.class), mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        BizException exception = assertThrows(BizException.class, () -> service.deleteById(1L, 1));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void deleteCleansAggregateAttachmentsInSameCommand() throws IOException {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntryMapper entryMapper = mock(PurchaseRequisitionEntryMapper.class);
        AttachmentService attachmentService = mock(AttachmentService.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.delete(any())).thenReturn(1);
        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mock(CurrentUserContext.class), mapper, entryMapper, attachmentService,
                mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        service.deleteById(1L, 2);

        verify(attachmentService).deleteForAggregate(
                PurchaseRequisitionResourceRegistration.RESOURCE_TYPE, "1");
        verify(entryMapper).delete(any());
        verify(mapper).delete(any());
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
                mock(CurrentUserContext.class), mapper, mock(PurchaseRequisitionEntryMapper.class),
                mock(AttachmentService.class), mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        BizException exception = assertThrows(BizException.class, () -> service.deleteById(1L, 2));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void deleteRejectsSubmittedBillBeforeDeletingEntries() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntryMapper entryMapper = mock(PurchaseRequisitionEntryMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SUBMITTED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mock(CurrentUserContext.class), mapper, entryMapper, mock(AttachmentService.class),
                mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        BizException exception = assertThrows(BizException.class, () -> service.deleteById(1L, 2));
        assertEquals(ResultEnum.BILL_STATUS_ERROR.getCode(), exception.getCode());
        verify(entryMapper, never()).delete(any());
        verify(mapper, never()).delete(any());
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
                mock(CurrentUserContext.class), mapper, mock(PurchaseRequisitionEntryMapper.class),
                mock(AttachmentService.class), mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        PurchaseRequisitionSubmitForm form = submitForm(1L, 1);
        BizException exception = assertThrows(BizException.class, () -> service.submit(form));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void submitReportsConflictWhenAtomicConditionNoLongerMatches() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntryMapper entryMapper = mock(PurchaseRequisitionEntryMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        PurchaseRequisitionEntity savedEntity = new PurchaseRequisitionEntity();
        savedEntity.setId(1L);
        savedEntity.setVersion(3);
        savedEntity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity, savedEntity);
        when(mapper.updateById(entity)).thenReturn(1);
        when(entryMapper.insert(any(PurchaseRequisitionEntryEntity.class))).thenReturn(1);
        when(mapper.update(any(PurchaseRequisitionEntity.class), any())).thenReturn(0);

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mock(CurrentUserContext.class), mapper, entryMapper, mock(AttachmentService.class),
                mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        PurchaseRequisitionSubmitForm form = submitForm(1L, 2);
        BizException exception = assertThrows(BizException.class, () -> service.submit(form));
        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void saveDoesNotSwallowEntryPersistenceFailure() {
        PurchaseRequisitionMapper mapper = mock(PurchaseRequisitionMapper.class);
        PurchaseRequisitionEntryMapper entryMapper = mock(PurchaseRequisitionEntryMapper.class);
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);
        when(entryMapper.insert(any(PurchaseRequisitionEntryEntity.class))).thenReturn(0);

        PurchaseRequisitionEntryForm entryForm = new PurchaseRequisitionEntryForm();
        entryForm.setMaterialName("测试物料");
        entryForm.setUnit("件");
        entryForm.setQuantity(BigDecimal.ONE);
        PurchaseRequisitionSaveForm form = new PurchaseRequisitionSaveForm();
        form.setId(1L);
        form.setVersion(2);
        form.setNumber("PR-001");
        form.setSubject("测试采购申请");
        form.setBizDate(LocalDate.of(2026, 7, 28));
        form.setEntries(List.of(entryForm));

        PurchaseRequisitionTxService service = new PurchaseRequisitionTxService(
                mock(CurrentUserContext.class), mapper, entryMapper, mock(AttachmentService.class),
                mock(NumberGeneratorAccessor.class), mock(PurchaseRequisitionDataScope.class));

        BizException exception = assertThrows(BizException.class, () -> service.save(form));
        assertEquals(ResultEnum.PERSISTENCE_ERROR.getCode(), exception.getCode());
        verify(mapper).updateById(entity);
        verify(entryMapper).delete(any());
    }

    private static PurchaseRequisitionSubmitForm submitForm(Long id, Integer version) {
        PurchaseRequisitionEntryForm entryForm = new PurchaseRequisitionEntryForm();
        entryForm.setMaterialName("测试物料");
        entryForm.setUnit("件");
        entryForm.setQuantity(BigDecimal.ONE);

        PurchaseRequisitionSubmitForm form = new PurchaseRequisitionSubmitForm();
        form.setId(id);
        form.setVersion(version);
        form.setNumber("PR-001");
        form.setSubject("测试采购申请");
        form.setBizDate(LocalDate.of(2026, 7, 28));
        form.setEntries(List.of(entryForm));
        return form;
    }
}
