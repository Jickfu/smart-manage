package sm.domain.sys.base.fileartifact.contract;

import sm.system.storage.FileStoragePurpose;
import java.time.Duration;

public interface FileArtifactGateway {
    FileArtifactReference create(FileStoragePurpose purpose, String originalName, String mimeType,
                                 byte[] content, Duration ttl, Integer maxDownloads);
}
