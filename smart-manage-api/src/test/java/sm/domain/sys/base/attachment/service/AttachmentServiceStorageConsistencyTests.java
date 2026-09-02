package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.system.security.context.CurrentOperatorProvider;
import sm.system.resource.AttachmentUploadPolicy;
import sm.system.resource.BusinessResourceRegistry;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStoreResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentServiceStorageConsistencyTests {
    private final AttachmentMapper mapper = mock(AttachmentMapper.class);
    private final FileStorageServiceFactory storageFactory = mock(FileStorageServiceFactory.class);
    private final FileStorageService storage = mock(FileStorageService.class);
    private final AttachmentTxService txService = mock(AttachmentTxService.class);
    private final BusinessResourceRegistry resourceRegistry = mock(BusinessResourceRegistry.class);
    private final AttachmentConfigService attachmentConfigService = mock(AttachmentConfigService.class);
    private final AttachmentService service = new AttachmentService(
            mapper, mock(BizAttachmentMapper.class), storageFactory, txService, resourceRegistry,
            mock(CurrentOperatorProvider.class), attachmentConfigService,
            new sm.domain.sys.base.user.service.UserReferenceService(mock(UserMapper.class)));

    @BeforeEach
    void setUp() {
        when(resourceRegistry.objectPrefix("sys.base.ui-config")).thenReturn("asset/sys/base/ui-config");
        when(attachmentConfigService.uploadPolicy())
                .thenReturn(new AttachmentUploadPolicy(1024, List.of("txt"), List.of("text/plain"), 24));
        when(storageFactory.getService()).thenReturn(storage);
        when(storage.getType()).thenReturn("LOCAL");
    }

    @Test
    void transactionCommitFailureTriggersStoredObjectCompensation() throws IOException {
        MultipartFile file = file("test.txt", "test");
        when(storage.store("asset/sys/base/ui-config", file))
                .thenReturn(FileStoreResult.of("stored.txt", "asset/sys/stored.txt", 4));
        when(txService.persistUpload(anyString(), anyString(), anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new TransactionSystemException("commit failed"));

        assertThrows(TransactionSystemException.class,
                () -> service.upload(file, "sys.base.ui-config"));

        verify(storage).delete("asset/sys/stored.txt");
    }

    @Test
    void compensationFailureIsAttachedToPersistenceFailureForTracing() throws IOException {
        MultipartFile file = file("test.txt", "test");
        when(storage.store("asset/sys/base/ui-config", file))
                .thenReturn(FileStoreResult.of("stored.txt", "asset/sys/stored.txt", 4));
        TransactionSystemException persistenceFailure = new TransactionSystemException("commit failed");
        when(txService.persistUpload(anyString(), anyString(), anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt())).thenThrow(persistenceFailure);
        org.mockito.Mockito.doThrow(new IOException("cleanup offline"))
                .when(storage).delete("asset/sys/stored.txt");

        TransactionSystemException thrown = assertThrows(TransactionSystemException.class,
                () -> service.upload(file, "sys.base.ui-config"));

        assertEquals(1, thrown.getSuppressed().length);
    }

    @Test
    void hashFailureBeforeStorageDoesNotCreateObject() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("broken.txt");
        when(file.getInputStream()).thenThrow(new IOException("read failed"));

        assertThrows(IOException.class, () -> service.upload(file, "sys.base.ui-config"));

        verify(storage, never()).store(anyString(), any(MultipartFile.class));
        verify(txService, never()).persistUpload(anyString(), anyString(), anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void deleteRunsStorageAndFinalStateAfterPendingTransactionReturns() throws IOException {
        AttachmentDeletionTarget target = new AttachmentDeletionTarget(1L, "LOCAL", "sys/stored.txt");
        when(txService.markPendingDelete(1L)).thenReturn(target);
        when(storageFactory.getService("LOCAL")).thenReturn(storage);

        service.deleteForAggregate(1L);

        var order = inOrder(txService, storage);
        order.verify(txService).markPendingDelete(1L);
        order.verify(storage).delete("sys/stored.txt");
        order.verify(txService).markDeleted(1L);
    }

    @Test
    void aggregateRollbackNeverDeletesStoredObject() throws IOException {
        AttachmentDeletionTarget target = new AttachmentDeletionTarget(1L, "LOCAL", "sys/stored.txt");
        when(txService.markPendingDelete(1L)).thenReturn(target);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.deleteForAggregate(1L);

            verify(storage, never()).delete(anyString());
            for (TransactionSynchronization synchronization
                    : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }
            verify(storage, never()).delete(anyString());
            verify(txService, never()).markDeleted(1L);
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void aggregateCommitDeletesStoredObjectAndConfirmsFinalState() throws IOException {
        AttachmentDeletionTarget target = new AttachmentDeletionTarget(1L, "LOCAL", "sys/stored.txt");
        when(txService.markPendingDelete(1L)).thenReturn(target);
        when(storageFactory.getService("LOCAL")).thenReturn(storage);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.deleteForAggregate(1L);

            verify(storage, never()).delete(anyString());
            for (TransactionSynchronization synchronization
                    : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(storage).delete("sys/stored.txt");
            verify(txService).markDeleted(1L);
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void storageDeleteFailureLeavesPendingStateForRetry() throws IOException {
        AttachmentDeletionTarget target = new AttachmentDeletionTarget(1L, "LOCAL", "sys/stored.txt");
        when(txService.markPendingDelete(1L)).thenReturn(target);
        when(storageFactory.getService("LOCAL")).thenReturn(storage);
        org.mockito.Mockito.doThrow(new IOException("offline")).when(storage).delete("sys/stored.txt");

        service.deleteForAggregate(1L);

        verify(txService, never()).markDeleted(1L);
    }

    @Test
    void cleanupReportsFinalStateFailureSoPendingEntryWillBeRetried() throws IOException {
        AttachmentEntity candidate = new AttachmentEntity();
        candidate.setId(1L);
        candidate.setStatus("PENDING_DELETE");
        candidate.setStorageType("LOCAL");
        candidate.setObjectKey("sys/stored.txt");
        when(mapper.selectList(any())).thenReturn(List.of(candidate));
        when(txService.markPendingDelete(1L))
                .thenReturn(new AttachmentDeletionTarget(1L, "LOCAL", "sys/stored.txt"));
        when(storageFactory.getService("LOCAL")).thenReturn(storage);
        org.mockito.Mockito.doThrow(new TransactionSystemException("status commit failed"))
                .when(txService).markDeleted(1L);

        assertEquals(1, service.cleanupExpiredAndPending());
        verify(storage).delete("sys/stored.txt");
    }

    private MultipartFile file(String originalName, String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalName);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getInputStream()).thenAnswer(ignored -> new ByteArrayInputStream(
                content.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return file;
    }
}
