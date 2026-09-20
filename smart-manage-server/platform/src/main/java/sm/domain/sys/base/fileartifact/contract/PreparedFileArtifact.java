package sm.domain.sys.base.fileartifact.contract;

import sm.system.storage.FileStoragePurpose;

import java.time.LocalDateTime;

/** 已写入物理存储、等待在业务数据库事务中登记的文件制品。 */
public record PreparedFileArtifact(
        FileStoragePurpose purpose,
        Long ownerUserId,
        String originalName,
        String storageType,
        String objectKey,
        String mimeType,
        long fileSize,
        LocalDateTime expiresAt,
        Integer maxDownloads) {
}
