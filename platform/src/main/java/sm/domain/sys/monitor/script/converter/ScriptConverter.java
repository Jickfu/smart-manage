package sm.domain.sys.monitor.script.converter;

import org.mapstruct.Mapper;
import sm.domain.sys.monitor.script.model.entity.ScriptEntity;
import sm.domain.sys.monitor.script.model.entity.ScriptLogEntity;
import sm.domain.sys.monitor.script.model.vo.ScriptDetailVO;
import sm.domain.sys.monitor.script.model.vo.ScriptLogDetailVO;
import sm.domain.sys.monitor.script.model.vo.ScriptLogListVO;
import sm.domain.sys.monitor.script.model.vo.ScriptListVO;
import sm.infrastructure.mapping.SmMapperConfig;

/** 脚本元数据纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
public interface ScriptConverter {
    ScriptListVO toListVO(ScriptEntity entity);
    ScriptDetailVO toDetailVO(ScriptEntity entity);
    ScriptLogListVO toLogListVO(ScriptLogEntity entity);
    ScriptLogDetailVO toLogDetailVO(ScriptLogEntity entity);
}
