package sm.domain.sys.monitor.sql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;

@Mapper
public interface SqlLogMapper extends BaseMapper<SqlLogEntity> {
}
