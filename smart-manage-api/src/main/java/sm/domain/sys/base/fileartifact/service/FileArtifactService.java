package sm.domain.sys.base.fileartifact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.FileArtifactGateway;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
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
    private static final long DOWNLOAD_CLAIM_LEASE_HOURS = 1;
    private final FileArtifactMapper mapper;
    private final FileArtifactTxService txService;
    private final FileStorageServiceFactory storageFactory;
    private final CurrentOperatorProvider currentOperatorProvider;

    @Override
    public FileArtifactReference create(FileStoragePurpose purpose, String originalName, String mimeType,
                                        byte[] content, Duration ttl, Integer maxDownloads) {
        PreparedFileArtifact prepared = prepare(purpose, originalName, mimeType, content, ttl, maxDownloads);
        try {
            return registerWithOwnTransaction(prepared);
        } catch (RuntimeException exception) {
            discardQuietly(prepared, exception);
            throw exception;
        }
    }

    /** 只写物理对象，不登记数据库；用于和调用方业务事务组合。 */
    public PreparedFileArtifact prepare(FileStoragePurpose purpose, String originalName, String mimeType,
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
        return new PreparedFileArtifact(purpose, ownerUserId, originalName, storage.getType(), stored.getStoredPath(),
                mimeType, stored.getFileSize(), LocalDateTime.now().plus(ttl), maxDownloads);
    }

    private FileArtifactReference registerWithOwnTransaction(PreparedFileArtifact prepared) {
        FileArtifactEntity entity = FileArtifactEntityFactory.fromPrepared(prepared);
        txService.insert(entity);
        return new FileArtifactReference(entity.getId(), prepared.originalName(), entity.getExpiresAt());
    }

    public void discardQuietly(PreparedFileArtifact prepared, Throwable originalFailure) {
        try {
            storageFactory.getService(prepared.storageType()).delete(prepared.objectKey());
        } catch (IOException | RuntimeException cleanupException) {
            originalFailure.addSuppressed(cleanupException);
            log.warn("未登记文件制品的物理对象补偿删除失败: purpose={}, objectKey={}",
                    prepared.purpose(), prepared.objectKey(), cleanupException);
        }
    }

    public FileArtifactDownloadClaim claim(Long id) {
        return txService.claim(id, currentOperatorProvider.getCurrentUserIdOrNull(), java.util.UUID.randomUUID().toString());
    }

    public void complete(FileArtifactDownloadClaim claim) {
        txService.complete(claim);
    }

    public void releaseQuietly(FileArtifactDownloadClaim claim, Throwable transferFailure) {
        try {
            txService.release(claim);
        } catch (RuntimeException releaseFailure) {
            transferFailure.addSuppressed(releaseFailure);
            log.warn("文件下载失败后的资格释放失败: id={}", claim.artifact().getId(), releaseFailure);
        }
    }

    public int cleanupExpiredAndPending() {
        List<FileArtifactEntity> candidates = mapper.selectList(new LambdaQueryWrapper<FileArtifactEntity>()
                // 下载响应是异步流；一次性制品先失效，再留出传输窗口，避免清理任务删除正在读取的对象。
                .nested(wrapper -> wrapper.eq(FileArtifactEntity::getStatus, "PENDING_DELETE")
                        .lt(FileArtifactEntity::getUpdateTime, LocalDateTime.now().minusMinutes(10)))
                .or(wrapper -> wrapper.eq(FileArtifactEntity::getStatus, "ACTIVE")
                        .lt(FileArtifactEntity::getExpiresAt, LocalDateTime.now())));
        List<FileArtifactEntity> staleClaims = mapper.selectList(new LambdaQueryWrapper<FileArtifactEntity>()
                .eq(FileArtifactEntity::getStatus, "DOWNLOADING")
                // 20MB 平台文件采用保守租约，避免正常慢速传输期间重新开放一次性凭据。
                .lt(FileArtifactEntity::getDownloadClaimedAt,
                        LocalDateTime.now().minusHours(DOWNLOAD_CLAIM_LEASE_HOURS)));
        int failed = 0;
        for (FileArtifactEntity staleClaim : staleClaims) {
            try {
                if (FileStoragePurpose.ONE_TIME_CREDENTIAL.name().equals(staleClaim.getPurpose())) {
                    // 一次性秘密的传输结果无法确认时必须 fail-closed，绝不能重新开放下载资格。
                    txService.invalidateStaleClaim(staleClaim);
                } else {
                    txService.releaseStaleClaim(staleClaim);
                }
            } catch (RuntimeException exception) {
                failed++;
                log.warn("文件制品超时下载声明处理失败: id={}, purpose={}",
                        staleClaim.getId(), staleClaim.getPurpose(), exception);
            }
        }
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
