package sm.domain.sys.base.attachment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.contract.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

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

    /** 在对象已经写入后，以短事务保存附件元数据和临时业务映射。 */
    public AttachmentVO persistUpload(String originalName, String objectKey, long fileSize, String mimeType,
            String fileExt, String storageType, String sha256, String bizType, int tempExpireHours) {
        boolean isTemp = bizType != null && !bizType.isBlank();
        AttachmentEntity entity = new AttachmentEntity();
        entity.setOriginalName(originalName);
        entity.setObjectKey(objectKey);
        entity.setFileSize(fileSize);
        entity.setMimeType(mimeType);
        entity.setFileExt(fileExt);
        entity.setStorageType(storageType);
        entity.setStatus(isTemp ? "TEMP" : "ACTIVE");
        entity.setUploadSessionId(isTemp ? UUID.randomUUID().toString() : null);
        entity.setExpiresAt(isTemp ? LocalDateTime.now().plusHours(tempExpireHours) : null);
        entity.setSha256(sha256);
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
            AttachmentVO attachment = assembleAttachmentVO(entity);
            attachment.setBusinessAttachmentId(biz.getId());
            return attachment;
        }
        return assembleAttachmentVO(entity);
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

    /** 短事务解除业务映射并标记待删除；外部对象由事务提交后的调用方处理。 */
    public AttachmentDeletionTarget markPendingDelete(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件 id 不能为空");
        }
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在：" + id);
        }
        if ("DELETED".equals(entity.getStatus())) {
            return new AttachmentDeletionTarget(id, entity.getStorageType(), entity.getObjectKey());
        }
        if ("PENDING_DELETE".equals(entity.getStatus())) {
            return new AttachmentDeletionTarget(id, entity.getStorageType(), entity.getObjectKey());
        }
        bizMapper.delete(new LambdaQueryWrapper<BizAttachmentEntity>()
                .eq(BizAttachmentEntity::getAttachmentId, id));
        entity.setStatus("PENDING_DELETE");
        if (mapper.updateById(entity) != 1) {
            throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
        }
        return new AttachmentDeletionTarget(id, entity.getStorageType(), entity.getObjectKey());
    }

    /** 对象已确认删除后，以独立短事务推进最终状态，避免复用已经提交的外层事务资源。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markDeleted(Long id) {
        AttachmentEntity entity = mapper.selectById(id);
        if (entity == null || "DELETED".equals(entity.getStatus())) return;
        if (!"PENDING_DELETE".equals(entity.getStatus())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "附件删除状态已变化");
        }
        entity.setStatus("DELETED");
        if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "附件删除状态更新失败");
        }
    }

    /** 更新附件在当前业务资源中的备注。 */
    public void updateRemark(Long businessAttachmentId, Long attachmentId, String remark) {
        BizAttachmentEntity mapping = bizMapper.selectById(businessAttachmentId);
        if (mapping == null || !attachmentId.equals(mapping.getAttachmentId())) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件缺少业务资源归属");
        }
        mapping.setRemark(remark == null || remark.isBlank() ? null : remark.trim());
        if (bizMapper.updateById(mapping) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "附件备注已被其他操作修改");
        }
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
        vo.setUploaderId(entity.getCreateUser());
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }

}

record AttachmentDeletionTarget(Long attachmentId, String storageType, String objectKey) {
}
