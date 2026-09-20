package sm.domain.sys.monitor.operatelog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;

@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLogEntity> {
}

