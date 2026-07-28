package sm.domain.sys.monitor.job.service;

import org.mapstruct.Mapper;
import sm.domain.sys.monitor.job.model.entity.JobLogEntity;
import sm.domain.sys.monitor.job.model.vo.JobLogListVO;
import sm.framework.mapping.SmMapperConfig;

/** 任务日志纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface JobLogConverter {
    JobLogListVO toListVO(JobLogEntity entity);
}
