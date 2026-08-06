package sm.system.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class S3FileStorageServiceTests {
    private final S3FileStorageService service = new S3FileStorageService(mock(FileStorageConfigProvider.class));

    @Test
    void objectKeyUsesRegisteredPrefixDateShardRandomNameAndSafeExtension() {
        String objectKey = service.objectKey("asset/sys/base/ui-config", "../../Logo.PNG");

        assertThat(objectKey).matches(
                "asset/sys/base/ui-config/\\d{4}/\\d{2}/[0-9a-f]{2}/[0-9a-f]{32}\\.png");
        assertThat(objectKey).doesNotContain("Logo", "..");
    }

    @Test
    void objectKeyRejectsRelativeDirectorySegments() {
        assertThrows(IllegalArgumentException.class,
                () -> service.objectKey("biz/sys/../secret", "report.pdf"));
    }
}
