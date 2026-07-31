package sm.domain.sys.scheduler.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.vo.JobDetailVO;
import sm.domain.sys.scheduler.model.vo.JobListVO;
import sm.framework.mapping.SmMapperConfig;

/** 定时任务持久化字段转换器，运行态信息由 Service 组装。 */
@Mapper(config = SmMapperConfig.class)
interface JobConverter {
    @Mapping(target = "lastExecuteTime", ignore = true)
    @Mapping(target = "lastExecuteStatus", ignore = true)
    JobListVO toListVO(JobEntity entity);

    @Mapping(target = "lastExecuteTime", ignore = true)
    @Mapping(target = "lastExecuteStatus", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    JobDetailVO toDetailVO(JobEntity entity);
}
