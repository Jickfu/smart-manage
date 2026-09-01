package sm.domain.sys.base.fileartifact.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class FileArtifactTxService {
    private final FileArtifactMapper mapper;

    void insert(FileArtifactEntity entity) {
        if (mapper.insert(entity) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "文件制品登记失败");
    }

    FileArtifactEntity consume(Long id, Long ownerUserId) {
        FileArtifactEntity entity = mapper.selectById(id);
        if (entity == null || !"ACTIVE".equals(entity.getStatus())
                || ownerUserId == null || !ownerUserId.equals(entity.getOwnerUserId())) {
            throw new BizException(ResultEnum.NOT_FOUND, "文件制品不存在或已失效");
        }
        if (!entity.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
            throw new BizException(ResultEnum.NOT_FOUND, "文件制品已过期");
        }
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        if (entity.getMaxDownloads() != null && entity.getDownloadCount() >= entity.getMaxDownloads()) {
            entity.setStatus("PENDING_DELETE");
        }
        if (mapper.updateById(entity) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "文件制品状态已变化");
        return entity;
    }

    void markDeleted(FileArtifactEntity entity) {
        entity.setStatus("DELETED");
        if (mapper.updateById(entity) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "文件制品删除状态已变化");
    }
}
