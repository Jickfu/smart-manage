package sm.domain.workflow.process.definition.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.workflow.process.definition.model.entity.WorkflowBindingEntity;
@Mapper
public interface WorkflowBindingMapper extends BaseMapper<WorkflowBindingEntity> { }
