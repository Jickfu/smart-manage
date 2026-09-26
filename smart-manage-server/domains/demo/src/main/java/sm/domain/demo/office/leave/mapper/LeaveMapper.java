package sm.domain.demo.office.leave.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sm.domain.demo.office.leave.model.entity.LeaveEntity;
import java.util.UUID;
@Mapper
public interface LeaveMapper extends BaseMapper<LeaveEntity> {
    @Select("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(CAST(#{requestId} AS text), 731905))")
    Integer lockSubmission(@Param("requestId") UUID requestId);
}
