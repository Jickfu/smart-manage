package sm.domain.sys.base.fileconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;

/**
 * @author Chekfu
 */
@Mapper
public interface FileConfigMapper extends BaseMapper<FileConfigEntity> {
    boolean existsStoredAttachment();
}
