package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactRegistrar;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.form.UserSaveForm;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserImportTxServiceTests {
    @Test
    void shouldCoordinateUserWriterAndArtifactRegistrarAsOneTransactionUnit() {
        UserWriter userWriter = mock(UserWriter.class);
        UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
        FileArtifactRegistrar registrar = mock(FileArtifactRegistrar.class);
        UserImportTxService service = new UserImportTxService(userWriter, userRoleMapper, registrar);
        UserSaveForm form = new UserSaveForm();
        PreparedFileArtifact prepared = mock(PreparedFileArtifact.class);
        FileArtifactReference reference = new FileArtifactReference(10L, "credential.xlsx",
                LocalDateTime.now().plusHours(1));
        when(userWriter.saveBatch(List.of(form))).thenReturn(List.of(1L));
        when(registrar.registerPrepared(prepared)).thenReturn(reference);

        UserImportTxService.BatchCommitResult result = service.commitBatch(List.of(form), prepared);

        assertThat(result.savedIds()).containsExactly(1L);
        assertThat(result.credentialFile()).isEqualTo(reference);
        verify(userWriter).saveBatch(List.of(form));
        verify(registrar).registerPrepared(prepared);
    }
}
