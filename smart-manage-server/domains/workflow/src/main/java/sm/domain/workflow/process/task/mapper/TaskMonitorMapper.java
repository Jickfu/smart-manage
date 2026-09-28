package sm.domain.workflow.process.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.workflow.process.task.model.vo.TaskMonitorRow;

@Mapper
public interface TaskMonitorMapper extends BaseMapper<sm.domain.workflow.process.instance.model.entity.WorkflowInstanceEntity> {
    Page<TaskMonitorRow> listPage(Page<TaskMonitorRow> page, @Param("keyword") String keyword);
}
