package sm.domain.sys.scheduler.converter;

import org.mapstruct.Mapper;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.model.vo.JobLogListVO;
import sm.domain.sys.scheduler.model.vo.JobLogDetailVO;
import sm.infrastructure.mapping.SmMapperConfig;

/** 任务日志纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
public interface JobLogConverter {
    JobLogListVO toListVO(JobLogEntity entity);

    JobLogDetailVO toDetailVO(JobLogEntity entity);
}
