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
    public AttachmentVO upload(MultipartFile file, String bizType) throws IOException {
        FileStorageService storage = storageFactory.getService();
        boolean isTemp = bizType != null && !bizType.isBlank();
        FileStoreResult result = isTemp ? storage.storeTemp(file) : storage.store("sys", file);
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        try {
            AttachmentEntity entity = new AttachmentEntity();
            entity.setOriginalName(originalName);
            entity.setStoredName(result.getStoredName());
            entity.setStoredPath(result.getStoredPath());
            entity.setFileSize(result.getFileSize());
            entity.setMimeType(file.getContentType());
            entity.setFileExt(ext);
            entity.setStorageType(storage.getType());
            entity.setIsTemp(isTemp);
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
        FileStorageService storage = storageFactory.getService();
        List<String> promotedPaths = new ArrayList<>();
        try {
            for (Long attachmentId : form.getAttachmentIds()) {
                AttachmentEntity entity = mapper.selectById(attachmentId);
                if (entity == null) {
                    throw new BizException(ResultEnum.NOT_FOUND, "附件不存在: " + attachmentId);
                }
                if (Boolean.TRUE.equals(entity.getIsTemp())) {
                    String newPath = storage.promote(entity.getStoredPath(), "biz/" + form.getBizType());
                    promotedPaths.add(newPath);
                    entity.setStoredPath(newPath);
                    entity.setIsTemp(false);
                    if (mapper.updateById(entity) != 1) {
                        throw new BizException(ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
                    }
                }
                BizAttachmentEntity bizEntity = selectBizByAttachmentId(attachmentId);
                if (bizEntity != null) {
                    bizEntity.setBizId(form.getBizId());
                    if (bizMapper.updateById(bizEntity) != 1) {
                        throw new BizException(ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
                    }
                } else {
                    BizAttachmentEntity newBiz = new BizAttachmentEntity();
                    newBiz.setBizType(form.getBizType());
                    newBiz.setBizId(form.getBizId());
                    newBiz.setAttachmentId(attachmentId);
                    newBiz.setSort(0);
                    if (bizMapper.insert(newBiz) != 1) {
                        throw new BizException(ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            for (int index = promotedPaths.size() - 1; index >= 0; index--) {
                moveForCompensation(storage, promotedPaths.get(index));
            }
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
        if (mapper.deleteById(id) != 1) {
            throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户删除");
        }
        FileStorageService storage = storageFactory.getService();
        String storedPath = entity.getStoredPath();
        // 数据库提交后再删除外部文件；失败会保留包含附件 ID 与路径的可恢复告警。
        TransactionUtil.afterCommit(() -> {
            try {
                storage.delete(storedPath);
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

    /** 上传结果需要拼装存储访问地址，因此不交给 MapStruct。 */
    private AttachmentVO assembleAttachmentVO(AttachmentEntity entity) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileSize(entity.getFileSize());
        vo.setMimeType(entity.getMimeType());
        vo.setFileExt(entity.getFileExt());
        vo.setIsTemp(entity.getIsTemp());
        vo.setUrl(storageFactory.getService().getAccessUrl(entity.getStoredPath()));
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

    private void moveForCompensation(FileStorageService storage, String promotedPath) {
        try {
            storage.move(promotedPath, "temp");
        } catch (IOException cleanupException) {
            log.error("附件提升失败且反向移动补偿失败: path={}", promotedPath, cleanupException);
        }
    }
}
