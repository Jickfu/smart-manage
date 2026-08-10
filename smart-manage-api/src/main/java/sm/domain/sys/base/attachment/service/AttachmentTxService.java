package sm.domain.sys.base.attachment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStoreResult;
import sm.system.util.TransactionUtil;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 附件事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class AttachmentTxService {
    private final AttachmentMapper mapper;
    private final BizAttachmentMapper bizMapper;
    private final FileStorageServiceFactory storageFactory;

    /** 上传附件：传 bizType 时存入临时目录（需 promote），否则直接存 sys 系统目录 */
    public AttachmentVO upload(MultipartFile file, String bizType, String objectPrefix, int tempExpireHours) throws IOException {
        FileStorageService storage = storageFactory.getService();
        boolean isTemp = bizType != null && !bizType.isBlank();
        FileStoreResult result = storage.store(objectPrefix, file);
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        try {
            AttachmentEntity entity = new AttachmentEntity();
            entity.setOriginalName(originalName);
            entity.setObjectKey(result.getStoredPath());
            entity.setFileSize(result.getFileSize());
            entity.setMimeType(file.getContentType());
            entity.setFileExt(ext);
            entity.setStorageType(storage.getType());
            entity.setStatus(isTemp ? "TEMP" : "ACTIVE");
            entity.setUploadSessionId(isTemp ? UUID.randomUUID().toString() : null);
            entity.setExpiresAt(isTemp ? LocalDateTime.now().plusHours(tempExpireHours) : null);
            entity.setSha256(sha256(file));
            if (mapper.insert(entity) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
            if (isTemp) {
                BizAttachmentEntity biz = new BizAttachmentEntity();
                biz.setBizType(bizType);
                biz.setBizId(null);
                biz.setAttachmentId(entity.getId());
                biz.setSort(0);
                if (bizMapper.insert(biz) != 1) {
                    throw new BizException(ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
                }
            }
            log.info("附件上传: id={}, name={}, temp={}", entity.getId(), originalName, isTemp);
            return assembleAttachmentVO(entity);
        } catch (RuntimeException exception) {
            deleteForCompensation(storage, result.getStoredPath(), "附件上传数据库写入失败");
            throw exception;
        }
    }

    /** 提升附件：关联业务单据 + 移出临时目录 */
    public void promote(AttachmentPromoteForm form) throws IOException {
        try {
            for (Long attachmentId : form.getAttachmentIds()) {
                AttachmentEntity entity = mapper.selectById(attachmentId);
                if (entity == null) {
                    throw new BizException(ResultEnum.NOT_FOUND, "附件不存在: " + attachmentId);
                }
                if (!"TEMP".equals(entity.getStatus()) && !"ACTIVE".equals(entity.getStatus())) {
                    throw new BizException(ResultEnum.NOT_FOUND, "附件不可用: " + attachmentId);
                }
                boolean temporary = "TEMP".equals(entity.getStatus());
                BizAttachmentEntity bizEntity = selectBizByAttachmentId(attachmentId);
                if (bizEntity == null) {
                    throw new BizException(ResultEnum.PERMISSION_ERROR, "附件缺少业务资源归属");
                }
                if (!form.getBizType().equals(bizEntity.getBizType())) {
                    throw new BizException(ResultEnum.PERMISSION_ERROR, "附件业务资源类型不匹配");
                }
                if (temporary && bizEntity.getBizId() != null) {
                    throw new BizException(ResultEnum.PERMISSION_ERROR, "临时附件已绑定业务单据");
                }
                if (!temporary && !form.getBizId().equals(bizEntity.getBizId())) {
                    throw new BizException(ResultEnum.PERMISSION_ERROR, "已确认附件不能绑定到其他业务单据");
                }
                if (temporary) {
                    entity.setStatus("ACTIVE");
                    entity.setExpiresAt(null);
                    entity.setUploadSessionId(null);
                    if (mapper.updateById(entity) != 1) {
                        throw new BizException(ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
                    }
                }
                if (temporary) {
                    bizEntity.setBizId(form.getBizId());
                    if (bizMapper.updateById(bizEntity) != 1) {
                        throw new BizException(ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
                    }
                }
            }
        } catch (RuntimeException exception) {
            throw exception;
        }
        log.info("附件提升: ids={}, bizType={}, bizId={}", form.getAttachmentIds(), form.getBizType(), form.getBizId());
    }

    /** 删除附件（物理文件 + 映射 + 元数据） */
    public void delete(Long id) throws IOException {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件 id 不能为空");
        }
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在：" + id);
        }
        bizMapper.delete(new LambdaQueryWrapper<BizAttachmentEntity>()
                .eq(BizAttachmentEntity::getAttachmentId, id));
        entity.setStatus("PENDING_DELETE");
        if (mapper.updateById(entity) != 1) {
            throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
        }
        FileStorageService storage = storageFactory.getService(entity.getStorageType());
        String storedPath = entity.getObjectKey();
        // 数据库提交后再删除外部文件；失败会保留包含附件 ID 与路径的可恢复告警。
        TransactionUtil.afterCommit(() -> {
            try {
                storage.delete(storedPath);
                entity.setStatus("DELETED");
                mapper.updateById(entity);
                log.info("附件删除: id={}, path={}", id, storedPath);
            } catch (IOException exception) {
                log.error("附件物理文件删除失败，需按附件ID和路径重试: id={}, path={}", id, storedPath, exception);
            }
        });
    }

    /** 按附件 ID 查询业务映射 */
    private BizAttachmentEntity selectBizByAttachmentId(Long attachmentId) {
        return bizMapper.selectOne(new LambdaQueryWrapper<BizAttachmentEntity>()
                .eq(BizAttachmentEntity::getAttachmentId, attachmentId));
    }

    private AttachmentVO assembleAttachmentVO(AttachmentEntity entity) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileSize(entity.getFileSize());
        vo.setMimeType(entity.getMimeType());
        vo.setFileExt(entity.getFileExt());
        vo.setIsTemp("TEMP".equals(entity.getStatus()));
        vo.setUploadSessionId(entity.getUploadSessionId());
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }

    private void deleteForCompensation(FileStorageService storage, String storedPath, String reason) {
        try {
            storage.delete(storedPath);
        } catch (IOException cleanupException) {
            log.error("{}，且补偿删除失败: path={}", reason, storedPath, cleanupException);
        }
    }

    private String sha256(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int readLength;
                while ((readLength = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, readLength);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持 SHA-256", exception);
        }
    }

    private String sanitizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) return "file";
        String normalized = originalName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1)
                .replaceAll("[\\x00-\\x1f\\x7f]", "_")
                .replace('"', '_').replace('\r', '_').replace('\n', '_');
        if (normalized.isBlank()) return "file";
        return normalized.length() > 255 ? normalized.substring(normalized.length() - 255) : normalized;
    }
}
