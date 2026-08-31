package sm.domain.sys.base.uiconfig.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UiConfigServiceTests {

    private final UiConfigMapper mapper = mock(UiConfigMapper.class);
    private final UiConfigTxService txService = mock(UiConfigTxService.class);
    private final UiConfigConverter converter = mock(UiConfigConverter.class);
    private final AttachmentService attachmentService = mock(AttachmentService.class);
    private final UiConfigService service = new UiConfigService(
            mapper, txService, converter, attachmentService);

    @Test
    void activeConfigUsesBusinessImageEndpointInsteadOfStorageUrl() {
        UiConfigEntity config = config(100L, 11L);
        UiConfigDetailVO detail = new UiConfigDetailVO();
        AttachmentReference attachment = new AttachmentReference();
        attachment.setId(11L);
        when(mapper.selectList(null)).thenReturn(List.of(config));
        when(converter.toDetailVO(config)).thenReturn(detail);
        when(attachmentService.listByIds(List.of(11L))).thenReturn(List.of(attachment));

        UiConfigDetailVO result = service.getActiveConfig();

        assertEquals("/sys/base/ui-config/image/login-banner?v=11", result.getLoginBanner());
    }

    @Test
    void activeImageOnlyReturnsConfiguredAggregateAttachment() {
        UiConfigEntity config = config(100L, 11L);
        AttachmentEntity attachment = new AttachmentEntity();
        when(mapper.selectList(null)).thenReturn(List.of(config));
        when(attachmentService.requireAggregateAttachment(11L, "sys.base.ui-config", "100"))
                .thenReturn(attachment);

        assertSame(attachment, service.requireActiveImage("login-banner"));
    }

    @Test
    void rejectsUnknownPublicImageType() {
        when(mapper.selectList(null)).thenReturn(List.of(config(100L, 11L)));

        assertThrows(BizException.class, () -> service.requireActiveImage("unknown"));
    }

    private UiConfigEntity config(Long id, Long loginBannerAttachmentId) {
        UiConfigEntity config = new UiConfigEntity();
        config.setId(id);
        config.setLoginBannerAttachmentId(loginBannerAttachmentId);
        return config;
    }
}
