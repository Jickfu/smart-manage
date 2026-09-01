package sm.domain.sys.base.fileartifact.service;

import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;

/** 文件制品登记模型的模块内工厂。 */
final class FileArtifactEntityFactory {
    private FileArtifactEntityFactory() {
    }

    static FileArtifactEntity fromPrepared(PreparedFileArtifact prepared) {
        FileArtifactEntity entity = new FileArtifactEntity();
        entity.setPurpose(prepared.purpose().name());
        entity.setOwnerUserId(prepared.ownerUserId());
        entity.setOriginalName(prepared.originalName());
        entity.setStorageType(prepared.storageType());
        entity.setObjectKey(prepared.objectKey());
        entity.setMimeType(prepared.mimeType());
        entity.setFileSize(prepared.fileSize());
        entity.setStatus("ACTIVE");
        entity.setExpiresAt(prepared.expiresAt());
        entity.setDownloadCount(0);
        entity.setMaxDownloads(prepared.maxDownloads());
        entity.setVersion(0);
        return entity;
    }
}
