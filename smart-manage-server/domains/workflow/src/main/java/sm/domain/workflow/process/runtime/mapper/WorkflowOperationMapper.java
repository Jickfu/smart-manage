package sm.domain.workflow.process.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.workflow.process.runtime.model.entity.WorkflowOperationEntity;

@Mapper
public interface WorkflowOperationMapper extends BaseMapper<WorkflowOperationEntity> { }
