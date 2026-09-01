package sm.domain.sys.base.fileartifact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactGateway;
import sm.system.exception.BizException;
import sm.system.helper.CurrentOperatorProvider;
import sm.system.response.ResultEnum;
import sm.system.storage.FileStoragePurpose;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStoreResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** 受管理文件制品入口，统一处理存储补偿、所有者、有效期和一次性下载。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileArtifactService implements FileArtifactGateway {
    private final FileArtifactMapper mapper;
    private final FileArtifactTxService txService;
    private final FileStorageServiceFactory storageFactory;
    private final CurrentOperatorProvider currentOperatorProvider;

    @Override
    public FileArtifactReference create(FileStoragePurpose purpose, String originalName, String mimeType,
                                        byte[] content, Duration ttl, Integer maxDownloads) {
        Long ownerUserId = currentOperatorProvider.getCurrentUserIdOrNull();
        if (ownerUserId == null) throw new BizException(ResultEnum.PERMISSION_ERROR, "未识别文件制品所有者");
        FileStorageService storage = storageFactory.getService();
        FileStoreResult stored;
        try {
            stored = storage.store(purpose.prefix(), originalName, mimeType, content.length,
                    new ByteArrayInputStream(content));
        } catch (IOException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "文件制品存储失败: " + exception.getMessage());
        }
        FileArtifactEntity entity = new FileArtifactEntity();
        entity.setPurpose(purpose.name());
        entity.setOwnerUserId(ownerUserId);
        entity.setOriginalName(originalName);
        entity.setStorageType(storage.getType());
        entity.setObjectKey(stored.getStoredPath());
        entity.setMimeType(mimeType);
        entity.setFileSize(stored.getFileSize());
        entity.setStatus("ACTIVE");
        entity.setExpiresAt(LocalDateTime.now().plus(ttl));
        entity.setDownloadCount(0);
        entity.setMaxDownloads(maxDownloads);
        entity.setVersion(0);
        try {
            txService.insert(entity);
        } catch (RuntimeException exception) {
            try { storage.delete(stored.getStoredPath()); } catch (IOException cleanupException) { exception.addSuppressed(cleanupException); }
            throw exception;
        }
        return new FileArtifactReference(entity.getId(), originalName, entity.getExpiresAt());
    }

    public FileArtifactEntity consume(Long id) {
        return txService.consume(id, currentOperatorProvider.getCurrentUserIdOrNull());
    }

    public int cleanupExpiredAndPending() {
        List<FileArtifactEntity> candidates = mapper.selectList(new LambdaQueryWrapper<FileArtifactEntity>()
                // 下载响应是异步流；一次性制品先失效，再留出传输窗口，避免清理任务删除正在读取的对象。
                .nested(wrapper -> wrapper.eq(FileArtifactEntity::getStatus, "PENDING_DELETE")
                        .lt(FileArtifactEntity::getUpdateTime, LocalDateTime.now().minusMinutes(10)))
                .or(wrapper -> wrapper.eq(FileArtifactEntity::getStatus, "ACTIVE")
                        .lt(FileArtifactEntity::getExpiresAt, LocalDateTime.now())));
        int failed = 0;
        for (FileArtifactEntity entity : candidates) {
            try {
                storageFactory.getService(entity.getStorageType()).delete(entity.getObjectKey());
                txService.markDeleted(entity);
            } catch (IOException | RuntimeException exception) {
                failed++;
                log.warn("文件制品清理失败，等待重试: id={}, purpose={}", entity.getId(), entity.getPurpose(), exception);
            }
        }
        return failed;
    }
}
