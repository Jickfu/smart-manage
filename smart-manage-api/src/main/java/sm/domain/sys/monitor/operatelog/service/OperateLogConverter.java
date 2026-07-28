package sm.domain.sys.monitor.operatelog.service;

import org.mapstruct.Mapper;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogDetailVO;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogListVO;
import sm.framework.mapping.SmMapperConfig;

/** 操作日志纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface OperateLogConverter {
    OperateLogListVO toListVO(OperateLogEntity entity);
    OperateLogDetailVO toDetailVO(OperateLogEntity entity);
}
