package sm.system.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class FileStorageServiceFactoryTests {

    @Test
    void persistedAttachmentUsesItsOwnStorageType() {
        LocalFileStorageService local = mock(LocalFileStorageService.class);
        FtpFileStorageService ftp = mock(FtpFileStorageService.class);
        S3FileStorageService s3 = mock(S3FileStorageService.class);
        FileStorageServiceFactory factory =
                new FileStorageServiceFactory(local, ftp, s3, mock(FileStorageConfigProvider.class));

        assertSame(local, factory.getService("LOCAL"));
        assertSame(ftp, factory.getService("FTP"));
        assertSame(s3, factory.getService("S3"));
    }
}
