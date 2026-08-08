package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.system.exception.BizException;
import sm.system.helper.CurrentOperatorProvider;
import sm.system.resource.BusinessResourceAccessPolicy;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistration;
import sm.system.resource.BusinessResourceRegistry;
import sm.system.storage.FileStorageServiceFactory;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentServiceAuthorizationTests {
    private static final String RESOURCE_TYPE = "sys.base.ui-config";

    private final AttachmentMapper mapper = mock(AttachmentMapper.class);
    private final BizAttachmentMapper bizMapper = mock(BizAttachmentMapper.class);
    private final FileStorageServiceFactory storageFactory = mock(FileStorageServiceFactory.class);
    private final AttachmentTxService txService = mock(AttachmentTxService.class);
    private final CurrentOperatorProvider currentOperatorProvider = mock(CurrentOperatorProvider.class);
    private final BusinessResourceAccessPolicy policy = mock(BusinessResourceAccessPolicy.class);
    private final AttachmentConfigService attachmentConfigService = mock(AttachmentConfigService.class);
    private final BusinessResourceRegistry registry = new BusinessResourceRegistry(List.of(registration()), attachmentConfigService);
    private final AttachmentService service = new AttachmentService(
            mapper, bizMapper, storageFactory, txService, registry, currentOperatorProvider, attachmentConfigService);

    @Test
    void temporaryAttachmentCanOnlyBeDownloadedByCreator() {
        AttachmentEntity attachment = attachment(true, 10L);
        when(mapper.selectById(1L)).thenReturn(attachment);
        when(currentOperatorProvider.getCurrentUserIdOrNull()).thenReturn(20L);

        assertThrows(BizException.class, () -> service.requireDownloadableAttachment(1L, "session"));
    }

    @Test
    void activeAttachmentDelegatesReadAuthorizationToBusinessResource() {
        AttachmentEntity attachment = attachment(false, 10L);
        BizAttachmentEntity mapping = new BizAttachmentEntity();
        mapping.setBizType(RESOURCE_TYPE);
        mapping.setBizId("100");
        mapping.setAttachmentId(1L);
        when(mapper.selectById(1L)).thenReturn(attachment);
        when(bizMapper.selectOne(any())).thenReturn(mapping);

        assertSame(attachment, service.requireDownloadableAttachment(1L, null));
        verify(policy).requireAllowed("100", BusinessResourceAction.READ);
    }

    @Test
    void activeAttachmentWithoutBusinessMappingIsDenied() {
        when(mapper.selectById(1L)).thenReturn(attachment(false, 10L));
        when(bizMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> service.requireDownloadableAttachment(1L, null));
    }

    @Test
    void aggregateAttachmentMustBeActiveAndBelongToRequestedResource() {
        AttachmentEntity attachment = attachment(false, 10L);
        BizAttachmentEntity mapping = new BizAttachmentEntity();
        mapping.setBizType(RESOURCE_TYPE);
        mapping.setBizId("100");
        mapping.setAttachmentId(1L);
        when(mapper.selectById(1L)).thenReturn(attachment);
        when(bizMapper.selectOne(any())).thenReturn(mapping);

        assertSame(attachment, service.requireAggregateAttachment(1L, RESOURCE_TYPE, "100"));
    }

    @Test
    void temporaryAttachmentCannotBeExposedAsAggregateResource() {
        when(mapper.selectById(1L)).thenReturn(attachment(true, 10L));

        assertThrows(
                BizException.class,
                () -> service.requireAggregateAttachment(1L, RESOURCE_TYPE, "100"));
    }

    @Test
    void mismatchedAggregateAttachmentIsDenied() {
        when(mapper.selectById(1L)).thenReturn(attachment(false, 10L));
        when(bizMapper.selectOne(any())).thenReturn(null);

        assertThrows(
                BizException.class,
                () -> service.requireAggregateAttachment(1L, RESOURCE_TYPE, "100"));
    }

    @Test
    void deletedAttachmentCannotBeExposedAsAggregateResource() {
        AttachmentEntity attachment = attachment(false, 10L);
        attachment.setStatus("DELETED");
        when(mapper.selectById(1L)).thenReturn(attachment);

        assertThrows(
                BizException.class,
                () -> service.requireAggregateAttachment(1L, RESOURCE_TYPE, "100"));
    }

    private AttachmentEntity attachment(boolean temporary, Long creatorId) {
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setId(1L);
        attachment.setStatus(temporary ? "TEMP" : "ACTIVE");
        attachment.setCreateUser(creatorId);
        if (temporary) {
            attachment.setUploadSessionId("session");
            attachment.setExpiresAt(java.time.LocalDateTime.now().plusHours(1));
        }
        return attachment;
    }

    private BusinessResourceRegistration registration() {
        return new BusinessResourceRegistration() {
            @Override
            public String resourceType() {
                return RESOURCE_TYPE;
            }

            @Override
            public BusinessResourceAccessPolicy accessPolicy() {
                return policy;
            }
        };
    }
}
