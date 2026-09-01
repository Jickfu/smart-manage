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
    void shouldMakeOneTimeArtifactUnavailableBeforeReturningIt() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        FileArtifactEntity entity = activeEntity();
        entity.setMaxDownloads(1);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        FileArtifactEntity consumed = new FileArtifactTxService(mapper).consume(1L, 7L);

        assertThat(consumed.getStatus()).isEqualTo("PENDING_DELETE");
        assertThat(consumed.getDownloadCount()).isEqualTo(1);
        verify(mapper).updateById(entity);
    }

    @Test
    void shouldRejectAnotherOwnerWithoutChangingState() {
        FileArtifactMapper mapper = mock(FileArtifactMapper.class);
        when(mapper.selectById(1L)).thenReturn(activeEntity());

        assertThatThrownBy(() -> new FileArtifactTxService(mapper).consume(1L, 8L))
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
