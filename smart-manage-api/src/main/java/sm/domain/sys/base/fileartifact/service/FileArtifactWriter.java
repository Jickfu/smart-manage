package sm.domain.sys.base.fileartifact.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.fileartifact.mapper.FileArtifactMapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 文件制品元数据的非事务写组件。 */
@Component
@RequiredArgsConstructor
class FileArtifactWriter {
    private final FileArtifactMapper mapper;

    void insert(FileArtifactEntity entity) {
        if (mapper.insert(entity) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "文件制品登记失败");
    }
}
