package sm.domain.sys.base.fileartifact.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.system.storage.FileStoragePurpose;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class FileArtifactRegistrarAdapterTests {
    @Test
    void shouldRejectRegistrationWithoutCallerTransaction() {
        FileArtifactWriter writer = mock(FileArtifactWriter.class);
        FileArtifactRegistrarAdapter registrar = new FileArtifactRegistrarAdapter(writer);
        try (MockedStatic<TransactionSynchronizationManager> transactionManager =
                     mockStatic(TransactionSynchronizationManager.class)) {
            transactionManager.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(false);

            assertThatThrownBy(() -> registrar.registerPrepared(prepared()))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("调用方");
        }
    }

    @Test
    void shouldUseWriterInsideCallerTransaction() {
        FileArtifactWriter writer = mock(FileArtifactWriter.class);
        FileArtifactRegistrarAdapter registrar = new FileArtifactRegistrarAdapter(writer);
        try (MockedStatic<TransactionSynchronizationManager> transactionManager =
                     mockStatic(TransactionSynchronizationManager.class)) {
            transactionManager.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            registrar.registerPrepared(prepared());

            verify(writer).insert(any());
        }
    }

    private PreparedFileArtifact prepared() {
        return new PreparedFileArtifact(FileStoragePurpose.ONE_TIME_CREDENTIAL, 1L, "credential.xlsx",
                "LOCAL", "credential.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                10L, LocalDateTime.now().plusHours(1), 1);
    }
}
