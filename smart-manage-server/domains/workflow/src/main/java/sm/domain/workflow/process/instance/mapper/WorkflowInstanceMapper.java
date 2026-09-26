package sm.domain.workflow.process.instance.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.workflow.process.instance.model.entity.WorkflowInstanceEntity;
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstanceEntity> { }
