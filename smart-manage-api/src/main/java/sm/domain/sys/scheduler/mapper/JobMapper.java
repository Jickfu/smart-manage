package sm.domain.sys.scheduler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.scheduler.model.form.JobListForm;
import sm.domain.sys.scheduler.model.vo.JobListVO;
import sm.domain.sys.scheduler.model.vo.JobExecutionSnapshotVO;
import sm.domain.sys.scheduler.model.vo.SchedulerCatalogRowVO;
import sm.system.query.ListSqlQuery;
import java.util.List;

/**
 * @author Chekfu
 */
@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {
    Page<JobListVO> selectListPage(Page<JobListVO> page, @Param("form") JobListForm form,
                                   @Param("listQuery") ListSqlQuery listQuery);
    List<SchedulerCatalogRowVO> selectCatalog();
    JobExecutionSnapshotVO selectExecutionSnapshot(@Param("id") Long id);
}
