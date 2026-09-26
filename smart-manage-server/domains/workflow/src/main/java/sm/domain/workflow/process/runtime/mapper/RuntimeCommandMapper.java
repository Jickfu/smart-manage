package sm.domain.workflow.process.runtime.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sm.domain.workflow.process.runtime.model.entity.RuntimeCommandEntity;
import java.util.UUID;
@Mapper
public interface RuntimeCommandMapper extends BaseMapper<RuntimeCommandEntity> {
    @Select("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(CAST(#{requestId} AS text), 731906))")
    Integer lockRequest(@Param("requestId") UUID requestId);
}
