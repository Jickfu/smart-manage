package sm.domain.sys.base.fileartifact.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.system.exception.BizException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileArtifactTxServiceTests {
    @Test
    void shouldClaimOneTimeArtifactWithoutConsumingDownload() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        entity.setMaxDownloads(1);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        FileArtifactDownloadClaim claim = new FileArtifactTxService(mapper).claim(1L, 7L, "claim-1");

        assertThat(claim.artifact().getStatus()).isEqualTo("DOWNLOADING");
        assertThat(claim.artifact().getDownloadCount()).isZero();
        assertThat(claim.claimToken()).isEqualTo("claim-1");
        verify(mapper).updateById(entity);
    }

    @Test
    void shouldConsumeOneTimeArtifactOnlyAfterTransferCompletes() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        entity.setMaxDownloads(1);
        entity.setStatus("DOWNLOADING");
        entity.setDownloadClaimToken("claim-1");
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        new FileArtifactTxService(mapper).complete(new FileArtifactDownloadClaim(entity, "claim-1"));

        assertThat(entity.getStatus()).isEqualTo("PENDING_DELETE");
        assertThat(entity.getDownloadCount()).isEqualTo(1);
        assertThat(entity.getDownloadClaimToken()).isNull();
    }

    @Test
    void shouldReleaseClaimWithoutConsumingDownloadWhenTransferFails() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        entity.setStatus("DOWNLOADING");
        entity.setDownloadClaimToken("claim-1");
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        new FileArtifactTxService(mapper).release(new FileArtifactDownloadClaim(entity, "claim-1"));

        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getDownloadCount()).isZero();
    }

    @Test
    void shouldInvalidateUncertainStaleOneTimeClaim() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        entity.setStatus("DOWNLOADING");
        entity.setDownloadClaimToken("claim-1");
        entity.setDownloadClaimedAt(LocalDateTime.now().minusHours(2));
        when(mapper.updateById(entity)).thenReturn(1);

        new FileArtifactTxService(mapper).invalidateStaleClaim(entity);

        assertThat(entity.getStatus()).isEqualTo("PENDING_DELETE");
        assertThat(entity.getDownloadClaimToken()).isNull();
        assertThat(entity.getDownloadClaimedAt()).isNull();
        verify(mapper).updateById(entity);
    }

    @Test
    void shouldAllowOnlyOneConcurrentClaimToUpdateState() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(0);

        assertThatThrownBy(() -> new FileArtifactTxService(mapper).claim(1L, 7L, "claim-1"))
                .isInstanceOf(BizException.class).hasMessageContaining("状态已变化");
    }

    @Test
    void shouldRejectAnotherOwnerWithoutChangingState() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        when(mapper.selectById(1L)).thenReturn(activeEntity());

        assertThatThrownBy(() -> new FileArtifactTxService(mapper).claim(1L, 8L, "claim-1"))
                .isInstanceOf(BizException.class);
    }

    private FileArtifactEntity activeEntity() {
        FileArtifactEntity entity = new FileArtifactEntity();
        entity.setId(1L);
        entity.setOwnerUserId(7L);
        entity.setStatus("ACTIVE");
        entity.setExpiresAt(LocalDateTime.now().plusHours(1));
        entity.setDownloadCount(0);
        entity.setVersion(0);
        return entity;
    }
}
