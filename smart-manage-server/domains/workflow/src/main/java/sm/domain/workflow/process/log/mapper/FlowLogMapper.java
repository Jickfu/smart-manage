package sm.domain.workflow.process.log.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.workflow.process.log.model.vo.FlowLogRow;

@Mapper
public interface FlowLogMapper {
    Page<FlowLogRow> listPage(Page<FlowLogRow> page, @Param("keyword") String keyword);
}
