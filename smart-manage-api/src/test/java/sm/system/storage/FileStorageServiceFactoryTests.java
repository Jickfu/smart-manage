package sm.system.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class FileStorageServiceFactoryTests {

    @Test
    void persistedAttachmentUsesItsOwnStorageType() {
        LocalFileStorageService local = mock(LocalFileStorageService.class);
        FtpFileStorageService ftp = mock(FtpFileStorageService.class);
        FileStorageServiceFactory factory =
                new FileStorageServiceFactory(local, ftp, mock(FileStorageConfigProvider.class));

        assertSame(local, factory.getService("LOCAL"));
        assertSame(ftp, factory.getService("FTP"));
    }
}
