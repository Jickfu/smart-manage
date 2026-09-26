package sm.domain.workflow.process.engine.warmflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.workflow.process.engine.WorkflowEngine;
import java.util.List;

/** 查询仍以引擎活动任务和历史事实为准，不维护第二套待办表。 */
@Mapper
public interface WarmFlowTaskQueryMapper {
    List<Long> selectInstances(@Param("actor") String actor, @Param("box") WorkflowEngine.Box box,
                              @Param("offset") int offset, @Param("limit") int limit);
    long countInstances(@Param("actor") String actor, @Param("box") WorkflowEngine.Box box);
}
