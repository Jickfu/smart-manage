package sm.domain.sys.base.uiconfig.service;

import org.junit.jupiter.api.Test;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UiConfigTxServiceTests {

    private final UiConfigMapper mapper = mock(UiConfigMapper.class);
    private final AttachmentGateway attachmentGateway = mock(AttachmentGateway.class);
    private final UiConfigTxService txService = new UiConfigTxService(mapper, attachmentGateway);

    @Test
    void transactionOwnerPromotesImagesBeforeSavingConfig() throws Exception {
        UiConfigSaveForm form = new UiConfigSaveForm();
        form.setLoginBannerAttachmentId(11L);
        form.setLoginLogoAttachmentId(12L);
        form.setHeaderLogoAttachmentId(11L);
        form.setAttachmentUploadSessions(java.util.Map.of(11L, "banner-session", 12L, "logo-session"));
        when(mapper.selectCount(null)).thenReturn(0L);
        when(mapper.insert(org.mockito.ArgumentMatchers.any(UiConfigEntity.class))).thenReturn(1);

        assertEquals(100L, txService.save(form, 100L));

        ArgumentCaptor<AttachmentPromoteCommand> commandCaptor =
                ArgumentCaptor.forClass(AttachmentPromoteCommand.class);
        verify(attachmentGateway).promoteForAggregate(commandCaptor.capture());
        AttachmentPromoteCommand command = commandCaptor.getValue();
        assertEquals(java.util.List.of(11L, 12L), command.getAttachmentIds());
        assertEquals("sys.base.ui-config", command.getBizType());
        assertEquals("100", command.getBizId());
        assertEquals(form.getAttachmentUploadSessions(), command.getUploadSessions());
    }

    @Test
    void skipsAttachmentPromotionWhenNoImageIsConfigured() {
        when(mapper.selectCount(null)).thenReturn(0L);
        when(mapper.insert(org.mockito.ArgumentMatchers.any(UiConfigEntity.class))).thenReturn(1);

        txService.save(new UiConfigSaveForm(), 100L);

        verifyNoInteractions(attachmentGateway);
    }

    @Test
    void rejectsStaleVersion() {
        UiConfigEntity entity = new UiConfigEntity();
        entity.setId(1L);
        entity.setVersion(2);
        when(mapper.selectById(1L)).thenReturn(entity);
        UiConfigSaveForm form = new UiConfigSaveForm();
        form.setId(1L);
        form.setVersion(1);

        assertThrows(BizException.class, () -> txService.save(form, 1L));
    }

    @Test
    void rejectsSecondSingletonRecord() {
        when(mapper.selectCount(null)).thenReturn(1L);

        assertThrows(BizException.class, () -> txService.save(new UiConfigSaveForm(), 1L));
    }

    @Test
    void nullableAttachmentIdsAlwaysParticipateInUpdates() throws NoSuchFieldException {
        for (String fieldName : new String[]{
                "loginBannerAttachmentId", "loginLogoAttachmentId", "headerLogoAttachmentId"}) {
            TableField tableField = UiConfigEntity.class.getDeclaredField(fieldName).getAnnotation(TableField.class);
            assertEquals(FieldStrategy.ALWAYS, tableField.updateStrategy());
        }
    }
}
