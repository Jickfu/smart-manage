package sm.domain.sys.scheduler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;

import java.util.List;

/**
 * @author Chekfu
 */
@Mapper
public interface JobLogMapper extends BaseMapper<JobLogEntity> {

    List<JobLogEntity> selectLatestByJobIds(@Param("jobIds") List<Long> jobIds);
}
