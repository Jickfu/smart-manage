package sm.domain.workflow.process.engine.warmflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 同一流程编码的创建、发布和发起共用事务锁，避免选中变化中的发布版本。 */
@Mapper
public interface WarmFlowCoordinationMapper {
    @Select("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(#{code}, 731904))")
    Integer lockDefinitionCode(@Param("code") String code);
}
