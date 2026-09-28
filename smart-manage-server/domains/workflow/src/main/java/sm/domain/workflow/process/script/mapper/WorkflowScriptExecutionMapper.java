package sm.domain.workflow.process.script.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.workflow.process.script.model.entity.WorkflowScriptExecutionEntity;

@Mapper
public interface WorkflowScriptExecutionMapper extends BaseMapper<WorkflowScriptExecutionEntity> { }
