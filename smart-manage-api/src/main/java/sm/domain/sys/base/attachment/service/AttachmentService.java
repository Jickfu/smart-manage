package sm.domain.sys.base.attachment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.model.vo.AttachmentDownloadAccessVO;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.helper.CurrentOperatorProvider;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistry;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附件服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentMapper mapper;
    private final BizAttachmentMapper bizMapper;
    private final FileStorageServiceFactory storageFactory;
    private final AttachmentTxService txService;
    private final BusinessResourceRegistry resourceRegistry;
    private final CurrentOperatorProvider currentOperatorProvider;
    private final AttachmentConfigService attachmentConfigService;

    /** 上传附件：传 bizType 时存入临时目录（需 promote），否则直接存 sys 系统目录 */
    @BizLog(value = "上传附件", recordRequest = false)
    public AttachmentVO upload(MultipartFile file, String bizType) throws IOException {
        if (bizType == null || bizType.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件业务资源类型不能为空");
        }
        resourceRegistry.validateUpload(bizType, file);
        return txService.upload(file, bizType, resourceRegistry.objectPrefix(bizType),
                attachmentConfigService.uploadPolicy().tempExpireHours());
    }

    /** 提升附件：关联业务单据 + 移出临时目录 */
    @BizLog("确认附件")
    public void promote(AttachmentPromoteForm form) throws IOException {
        requireTemporaryAttachmentOwnership(form.getAttachmentIds(), form.getUploadSessions());
        resourceRegistry.requireAllowed(form.getBizType(), form.getBizId(), BusinessResourceAction.ATTACH);
        txService.promote(form);
    }

    /**
     * 供已经完成自身权限、状态和目标 ID 校验的业务命令内部确认附件，避免嵌套记录第二条业务日志。
     * 新聚合保存前目标记录尚不存在，因此这里校验注册类型和临时附件所有权，不反查目标记录。
     */
    public void promoteForAggregate(AttachmentPromoteForm form) throws IOException {
        requireTemporaryAttachmentOwnership(form.getAttachmentIds(), form.getUploadSessions());
        resourceRegistry.requireRegistered(form.getBizType());
        txService.promote(form);
    }

    /** 删除附件（物理文件 + 映射 + 元数据） */
    @BizLog("删除附件")
    public void delete(Long id, String uploadSessionId) throws IOException {
        requireAttachmentAccess(id, BusinessResourceAction.DELETE, uploadSessionId);
        txService.delete(id);
    }

    /** 已完成自身权限和状态校验的业务命令内部清理附件。 */
    public void deleteForAggregate(Long id) throws IOException {
        txService.delete(id);
    }

    /** 已完成主聚合删除权限校验后，清理其全部正式附件。 */
    public void deleteForAggregate(String bizType, String bizId) throws IOException {
        for (AttachmentEntity entity : mapper.selectByBiz(bizType, bizId)) {
            txService.delete(entity.getId());
        }
    }

    /** 按业务单据查询附件列表 */
    public List<AttachmentVO> listByBiz(String bizType, String bizId) {
        resourceRegistry.requireAllowed(bizType, bizId, BusinessResourceAction.READ);
        List<AttachmentEntity> entities = mapper.selectByBiz(bizType, bizId);
        return entities.stream().map(this::assembleAttachmentVO).collect(Collectors.toList());
    }

    /** 列出可用附件；删除中和已删除记录不再属于可引用资源。 */
    public List<AttachmentVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(ids).stream()
                .filter(entity -> "TEMP".equals(entity.getStatus()) || "ACTIVE".equals(entity.getStatus()))
                .map(this::assembleAttachmentVO)
                .collect(Collectors.toList());
    }

    public AttachmentEntity requireDownloadableAttachment(Long id, String uploadSessionId) {
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在");
        }
        requireAttachmentAccess(entity, BusinessResourceAction.READ, uploadSessionId);
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
}
