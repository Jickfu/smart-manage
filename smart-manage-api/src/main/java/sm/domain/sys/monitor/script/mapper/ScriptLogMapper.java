package sm.domain.sys.monitor.script.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.monitor.script.model.entity.ScriptLogEntity;

@Mapper
public interface ScriptLogMapper extends BaseMapper<ScriptLogEntity> {
}
