package sm.domain.sys.monitor.sql.service;

import org.mapstruct.Mapper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;
import sm.domain.sys.monitor.sql.model.vo.SqlLogDetailVO;
import sm.domain.sys.monitor.sql.model.vo.SqlLogListVO;
import sm.framework.mapping.SmMapperConfig;

/** SQL 执行日志纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface SqlLogConverter {
    SqlLogListVO toListVO(SqlLogEntity entity);
    SqlLogDetailVO toDetailVO(SqlLogEntity entity);
}
