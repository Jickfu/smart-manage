package sm.domain.sys.base.fileartifact.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.system.helper.CurrentOperatorProvider;
import sm.system.storage.FileStoragePurpose;
import sm.system.storage.FileStorageServiceFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileArtifactServiceTests {
    @Test
    void staleOneTimeCredentialClaimMustBeInvalidatedInsteadOfReopened() {
        Fixture fixture = fixture(staleClaim(FileStoragePurpose.ONE_TIME_CREDENTIAL));

        assertThat(fixture.service().cleanupExpiredAndPending()).isZero();

        verify(fixture.txService()).invalidateStaleClaim(fixture.staleClaim());
        verify(fixture.txService(), never()).releaseStaleClaim(fixture.staleClaim());
    }

    @Test
    void staleOrdinaryArtifactClaimMayBeReopened() {
        Fixture fixture = fixture(staleClaim(FileStoragePurpose.DATA_EXPORT_RESULT));

        assertThat(fixture.service().cleanupExpiredAndPending()).isZero();

        verify(fixture.txService()).releaseStaleClaim(fixture.staleClaim());
        verify(fixture.txService(), never()).invalidateStaleClaim(fixture.staleClaim());
    }

    private Fixture fixture(FileArtifactEntity staleClaim) {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactTxService txService = mock(FileArtifactTxService.class);
        when(mapper.selectList(any())).thenReturn(List.of(), List.of(staleClaim));
        FileArtifactService service = new FileArtifactService(mapper, txService,
                mock(FileStorageServiceFactory.class), mock(CurrentOperatorProvider.class));
        return new Fixture(service, txService, staleClaim);
    }

    private FileArtifactEntity staleClaim(FileStoragePurpose purpose) {
        FileArtifactEntity entity = new FileArtifactEntity();
        entity.setId(1L);
        entity.setPurpose(purpose.name());
        entity.setStatus("DOWNLOADING");
        return entity;
    }

    private record Fixture(FileArtifactService service, FileArtifactTxService txService,
                           FileArtifactEntity staleClaim) {
    }
}
