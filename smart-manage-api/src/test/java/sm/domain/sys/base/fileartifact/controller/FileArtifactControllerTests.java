package sm.domain.sys.base.fileartifact.controller;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.domain.sys.base.fileartifact.service.FileArtifactDownloadClaim;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;
import sm.system.form.IdForm;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileArtifactControllerTests {
    @Test
    void successfulTransferCompletesClaim() throws Exception {
        Fixture fixture = fixture();
        when(fixture.storage().openStream("credential.xlsx"))
                .thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        fixture.controller().download(idForm()).getBody().writeTo(new ByteArrayOutputStream());

        verify(fixture.service()).complete(fixture.claim());
    }

    @Test
    void openStreamFailureReleasesClaim() throws Exception {
        Fixture fixture = fixture();
        IOException failure = new IOException("storage unavailable");
        when(fixture.storage().openStream("credential.xlsx")).thenThrow(failure);

        assertThrows(IOException.class,
                () -> fixture.controller().download(idForm()).getBody().writeTo(new ByteArrayOutputStream()));

        verify(fixture.service()).releaseQuietly(fixture.claim(), failure);
    }

    @Test
    void transferFailureReleasesClaim() throws Exception {
        Fixture fixture = fixture();
        IOException failure = new IOException("connection interrupted");
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }
        };
        when(fixture.storage().openStream("credential.xlsx")).thenReturn(failingStream);

        assertThrows(IOException.class,
                () -> fixture.controller().download(idForm()).getBody().writeTo(new ByteArrayOutputStream()));

        verify(fixture.service()).releaseQuietly(fixture.claim(), failure);
    }

    @Test
    void completeFailureMustNotReleaseTransferredOneTimeArtifact() throws Exception {
        Fixture fixture = fixture();
        RuntimeException failure = new RuntimeException("database unavailable");
        when(fixture.storage().openStream("credential.xlsx"))
                .thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        org.mockito.Mockito.doThrow(failure).when(fixture.service()).complete(fixture.claim());

        assertThrows(RuntimeException.class,
                () -> fixture.controller().download(idForm()).getBody().writeTo(new ByteArrayOutputStream()));

        verify(fixture.service()).complete(fixture.claim());
        verify(fixture.service(), never()).releaseQuietly(fixture.claim(), failure);
    }

    private Fixture fixture() {
        FileArtifactService service = mock(FileArtifactService.class);
        FileStorageServiceFactory storageFactory = mock(FileStorageServiceFactory.class);
        FileStorageService storage = mock(FileStorageService.class);
        FileArtifactEntity entity = new FileArtifactEntity();
        entity.setId(1L);
        entity.setStorageType("LOCAL");
        entity.setObjectKey("credential.xlsx");
        entity.setOriginalName("credential.xlsx");
        entity.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        entity.setFileSize(3L);
        FileArtifactDownloadClaim claim = new FileArtifactDownloadClaim(entity, "claim-1");
        when(service.claim(1L)).thenReturn(claim);
        when(storageFactory.getService("LOCAL")).thenReturn(storage);
        return new Fixture(new FileArtifactController(service, storageFactory), service, storage, claim);
    }

    private IdForm idForm() {
        IdForm form = new IdForm();
        form.setId(1L);
        return form;
    }

    private record Fixture(FileArtifactController controller, FileArtifactService service,
                           FileStorageService storage, FileArtifactDownloadClaim claim) {
    }
}
