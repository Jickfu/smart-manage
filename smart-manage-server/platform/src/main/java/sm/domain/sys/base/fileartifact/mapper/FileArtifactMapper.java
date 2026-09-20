package sm.domain.sys.base.fileartifact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.fileartifact.model.entity.FileArtifactEntity;

@Mapper
public interface FileArtifactMapper extends BaseMapper<FileArtifactEntity> {
}
