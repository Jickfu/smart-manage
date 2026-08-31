package sm.domain.sys.base.attachment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.system.aop.log.BizLog;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import sm.domain.sys.base.attachment.model.vo.AttachmentDownloadAccessVO;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStoreResult;
import sm.system.helper.CurrentOperatorProvider;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistry;
import sm.system.util.TransactionUtil;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 附件服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentService implements AttachmentGateway {
    private static final Set<String> PREVIEWABLE_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");
    private final AttachmentMapper mapper;
    private final BizAttachmentMapper bizMapper;
    private final FileStorageServiceFactory storageFactory;
    private final AttachmentTxService txService;
    private final BusinessResourceRegistry resourceRegistry;
    private final CurrentOperatorProvider currentOperatorProvider;
    private final AttachmentConfigService attachmentConfigService;
    private final UserMapper userMapper;

    /** 上传附件：传 bizType 时存入临时目录（需 promote），否则直接存 sys 系统目录 */
    @BizLog(value = "上传附件", recordRequest = false)
    public AttachmentReference upload(MultipartFile file, String bizType) throws IOException {
        if (bizType == null || bizType.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件业务资源类型不能为空");
        }
        resourceRegistry.validateUpload(bizType, file);
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String fileExt = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : "";
        String sha256 = sha256(file);
        FileStorageService storage = storageFactory.getService();
        FileStoreResult storedObject = storage.store(resourceRegistry.objectPrefix(bizType), file);
        AttachmentReference attachment;
        try {
            attachment = txService.persistUpload(
                    originalName, storedObject.getStoredPath(), storedObject.getFileSize(), file.getContentType(),
                    fileExt, storage.getType(), sha256, bizType,
                    attachmentConfigService.uploadPolicy().tempExpireHours());
        } catch (RuntimeException exception) {
            deleteUploadForCompensation(storage, storedObject.getStoredPath(), exception);
            throw exception;
        }
        log.info("附件上传: id={}, name={}, temp={}", attachment.getId(), originalName, attachment.getIsTemp());
        attachUploaderNames(List.of(attachment));
        return attachment;
    }

    /** 提升附件：关联业务单据 + 移出临时目录 */
    @BizLog("确认附件")
    public void promote(AttachmentPromoteCommand command) throws IOException {
        requireTemporaryAttachmentOwnership(command.getAttachmentIds(), command.getUploadSessions());
        resourceRegistry.requireAllowed(command.getBizType(), command.getBizId(), BusinessResourceAction.ATTACH);
        txService.promote(command);
    }

    /**
     * 供已经完成自身权限、状态和目标 ID 校验的业务命令内部确认附件，避免嵌套记录第二条业务日志。
     * 新聚合保存前目标记录尚不存在，因此这里校验注册类型和临时附件所有权，不反查目标记录。
     */
    @Override
    public void promoteForAggregate(AttachmentPromoteCommand command) throws IOException {
        requireTemporaryAttachmentOwnership(command.getAttachmentIds(), command.getUploadSessions());
        resourceRegistry.requireRegistered(command.getBizType());
        txService.promote(command);
    }

    /** 删除附件（物理文件 + 映射 + 元数据） */
    @BizLog("删除附件")
    public void delete(Long id, String uploadSessionId) throws IOException {
        requireAttachmentAccess(id, BusinessResourceAction.DELETE, uploadSessionId);
        deleteStoredObjectAfterCommit(txService.markPendingDelete(id));
    }

    /** 已完成自身权限和状态校验的业务命令内部清理附件。 */
    public void deleteForAggregate(Long id) throws IOException {
        deleteStoredObjectAfterCommit(txService.markPendingDelete(id));
    }

    /** 已完成主聚合删除权限校验后，清理其全部正式附件。 */
    @Override
    public void deleteForAggregate(String bizType, String bizId) throws IOException {
        for (AttachmentEntity entity : mapper.selectByBiz(bizType, bizId)) {
            deleteStoredObjectAfterCommit(txService.markPendingDelete(entity.getId()));
        }
    }

    /** Quartz 调用的幂等清理入口；失败记录保持 PENDING_DELETE，供下次继续重试。 */
    public int cleanupExpiredAndPending() {
        List<AttachmentEntity> candidates = mapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttachmentEntity>()
                        .and(wrapper -> wrapper
                                .eq(AttachmentEntity::getStatus, "PENDING_DELETE")
                                .or(expired -> expired.eq(AttachmentEntity::getStatus, "TEMP")
                                        .lt(AttachmentEntity::getExpiresAt, LocalDateTime.now())))
                        .orderByAsc(AttachmentEntity::getId));
        int failed = 0;
        for (AttachmentEntity candidate : candidates) {
            try {
                AttachmentDeletionTarget target = txService.markPendingDelete(candidate.getId());
                if (!deleteStoredObject(target)) failed++;
            } catch (RuntimeException exception) {
                failed++;
                log.warn("附件清理失败，等待下次幂等重试: id={}, storageType={}",
                        candidate.getId(), candidate.getStorageType(), exception);
            }
        }
        log.info("附件清理完成: candidates={}, failed={}", candidates.size(), failed);
        return failed;
    }

    /** 按业务单据查询附件列表 */
    @Override
    public List<AttachmentReference> listByBiz(String bizType, String bizId) {
        resourceRegistry.requireAllowed(bizType, bizId, BusinessResourceAction.READ);
        List<AttachmentEntity> entities = mapper.selectByBiz(bizType, bizId);
        List<BizAttachmentEntity> mappings = bizMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizAttachmentEntity>()
                        .eq(BizAttachmentEntity::getBizType, bizType)
                        .eq(BizAttachmentEntity::getBizId, bizId));
        return assembleAttachmentReferences(entities, mappings);
    }

    /** 列出可用附件；删除中和已删除记录不再属于可引用资源。 */
    public List<AttachmentReference> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<AttachmentEntity> entities = mapper.selectByIds(ids).stream()
                .filter(entity -> "TEMP".equals(entity.getStatus()) || "ACTIVE".equals(entity.getStatus()))
                .toList();
        return assembleAttachmentReferences(entities, selectMappings(entities));
    }

    /** 更新附件备注；正式附件继承业务资源维护权限，临时附件校验上传会话。 */
    @BizLog("更新附件备注")
    public AttachmentReference updateRemark(Long id, Long businessAttachmentId, String remark, String uploadSessionId) {
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        requireAttachmentAccess(entity, BusinessResourceAction.ATTACH, uploadSessionId);
        txService.updateRemark(businessAttachmentId, id, remark);
        BizAttachmentEntity mapping = new BizAttachmentEntity();
        mapping.setId(businessAttachmentId);
        mapping.setAttachmentId(id);
        mapping.setRemark(remark == null || remark.isBlank() ? null : remark.trim());
        return assembleAttachmentReferences(List.of(entity), List.of(mapping)).getFirst();
    }

    public AttachmentEntity requireDownloadableAttachment(Long id, String uploadSessionId) {
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        requireAttachmentAccess(entity, BusinessResourceAction.READ, uploadSessionId);
        return entity;
    }

    /** 预览仅允许浏览器可安全内嵌展示的图片和 PDF。 */
    public AttachmentEntity requirePreviewableAttachment(Long id, String uploadSessionId) {
        AttachmentEntity entity = requireDownloadableAttachment(id, uploadSessionId);
        String mimeType = entity.getMimeType();
        if (!PREVIEWABLE_IMAGE_TYPES.contains(mimeType) && !"application/pdf".equals(mimeType)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "该文件类型不支持在线预览");
        }
        return entity;
    }

    /** 权限校验必须先于短时直连地址签发，Local/FTP 返回空地址并由后端代理下载。 */
    public AttachmentDownloadAccessVO createDownloadAccess(Long id, String uploadSessionId) {
        AttachmentEntity entity = requireDownloadableAttachment(id, uploadSessionId);
        String directUrl = storageFactory.getService(entity.getStorageType())
                .createAuthorizedDownloadUrl(entity.getObjectKey());
        return new AttachmentDownloadAccessVO(directUrl);
    }

    /** 供业务聚合读取自身正式附件，必须同时匹配业务类型和业务 ID。 */
    public AttachmentEntity requireAggregateAttachment(Long id, String bizType, String bizId) {
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null || !"ACTIVE".equals(entity.getStatus())) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        BizAttachmentEntity mapping = bizMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizAttachmentEntity>()
                        .eq(BizAttachmentEntity::getAttachmentId, id)
                        .eq(BizAttachmentEntity::getBizType, bizType)
                        .eq(BizAttachmentEntity::getBizId, bizId));
        if (mapping == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "附件不属于指定业务资源");
        }
        return entity;
    }

    private void requireTemporaryAttachmentOwnership(List<Long> attachmentIds, java.util.Map<Long, String> uploadSessions) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        List<AttachmentEntity> entities = mapper.selectByIds(attachmentIds);
        if (entities.size() != attachmentIds.size()) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        for (AttachmentEntity entity : entities) {
            if (!"TEMP".equals(entity.getStatus()) && !"ACTIVE".equals(entity.getStatus())) {
                throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
            }
            if ("TEMP".equals(entity.getStatus())) {
                requireTemporaryAccess(entity, uploadSessions == null ? null : uploadSessions.get(entity.getId()));
            }
        }
    }

    private void requireAttachmentAccess(Long attachmentId, BusinessResourceAction action, String uploadSessionId) {
        AttachmentEntity entity = mapper.selectById(attachmentId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        requireAttachmentAccess(entity, action, uploadSessionId);
    }

    private void requireAttachmentAccess(AttachmentEntity entity, BusinessResourceAction action, String uploadSessionId) {
        if ("TEMP".equals(entity.getStatus())) {
            requireTemporaryAccess(entity, uploadSessionId);
            return;
        }
        BizAttachmentEntity mapping = bizMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizAttachmentEntity>()
                .eq(BizAttachmentEntity::getAttachmentId, entity.getId()));
        if (mapping == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "附件缺少业务资源归属");
        }
        resourceRegistry.requireAllowed(mapping.getBizType(), mapping.getBizId(), action);
    }

    private void requireCreator(AttachmentEntity entity) {
        Long currentUserId = currentOperatorProvider.getCurrentUserIdOrNull();
        if (currentUserId == null || !currentUserId.equals(entity.getCreateUser())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "无权访问该临时附件");
        }
    }

    private void requireTemporaryAccess(AttachmentEntity entity, String uploadSessionId) {
        requireCreator(entity);
        if (uploadSessionId == null || !uploadSessionId.equals(entity.getUploadSessionId())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "上传会话不匹配");
        }
        if (entity.getExpiresAt() == null || !entity.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "临时附件已过期");
        }
    }

    private AttachmentReference assembleAttachmentReference(AttachmentEntity entity) {
        AttachmentReference vo = new AttachmentReference();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileSize(entity.getFileSize());
        vo.setMimeType(entity.getMimeType());
        vo.setFileExt(entity.getFileExt());
        vo.setIsTemp("TEMP".equals(entity.getStatus()));
        vo.setUploadSessionId(entity.getUploadSessionId());
        vo.setUploaderId(entity.getCreateUser());
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }

    private List<AttachmentReference> assembleAttachmentReferences(
            List<AttachmentEntity> entities, List<BizAttachmentEntity> mappings) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<AttachmentReference> attachments = entities.stream().map(this::assembleAttachmentReference).toList();
        Map<Long, BizAttachmentEntity> mappingsByAttachmentId = mappings.stream()
                .collect(Collectors.toMap(BizAttachmentEntity::getAttachmentId,
                        Function.identity(), (left, right) -> left));
        for (AttachmentReference attachment : attachments) {
            BizAttachmentEntity mapping = mappingsByAttachmentId.get(attachment.getId());
            if (mapping != null) {
                attachment.setBusinessAttachmentId(mapping.getId());
                attachment.setRemark(mapping.getRemark());
            }
        }
        attachUploaderNames(attachments);
        return attachments;
    }

    private List<BizAttachmentEntity> selectMappings(List<AttachmentEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        return bizMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizAttachmentEntity>()
                        .in(BizAttachmentEntity::getAttachmentId,
                                entities.stream().map(AttachmentEntity::getId).toList()));
    }

    private void attachUploaderNames(List<AttachmentReference> attachments) {
        List<Long> uploaderIds = attachments.stream()
                .map(AttachmentReference::getUploaderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uploaderIds.isEmpty()) {
            return;
        }
        Map<Long, UserEntity> users = userMapper.selectBatchIds(uploaderIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        for (AttachmentReference attachment : attachments) {
            UserEntity uploader = users.get(attachment.getUploaderId());
            attachment.setUploaderName(uploader == null ? null : uploader.getName());
        }
    }

    private boolean deleteStoredObject(AttachmentDeletionTarget target) {
        try {
            storageFactory.getService(target.storageType()).delete(target.objectKey());
            txService.markDeleted(target.attachmentId());
            log.info("附件删除: id={}, path={}", target.attachmentId(), target.objectKey());
            return true;
        } catch (IOException | RuntimeException exception) {
            log.error("附件物理删除或状态确认失败，等待后台重试: id={}, path={}",
                    target.attachmentId(), target.objectKey(), exception);
            return false;
        }
    }

    /** 当前事务回滚时不触碰对象存储；无外层事务时立即执行，保持独立删除入口的既有语义。 */
    private void deleteStoredObjectAfterCommit(AttachmentDeletionTarget target) {
        TransactionUtil.afterCommit(() -> deleteStoredObject(target));
    }

    private void deleteUploadForCompensation(
            FileStorageService storage, String objectKey, RuntimeException persistenceException) {
        try {
            storage.delete(objectKey);
        } catch (IOException | RuntimeException cleanupException) {
            persistenceException.addSuppressed(cleanupException);
            log.error("附件元数据事务失败且对象补偿删除失败: path={}", objectKey, cleanupException);
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
