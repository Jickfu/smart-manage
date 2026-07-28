package sm.domain.sys.monitor.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.job.model.entity.JobLogEntity;
import sm.domain.sys.monitor.job.model.form.JobLogListForm;
import sm.domain.sys.monitor.job.model.vo.JobLogListVO;
import sm.domain.sys.monitor.job.mapper.JobLogMapper;
import sm.system.response.PageData;

import java.util.List;

/**
 * 执行实例/执行日志 Service
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobLogService {

    private final JobLogMapper mapper;
    private final JobLogConverter converter;

    public PageData<JobLogListVO> listPage(JobLogListForm form) {
        LambdaQueryWrapper<JobLogEntity> qw = new LambdaQueryWrapper<JobLogEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            qw.like(JobLogEntity::getJobName, "%" + form.getKeyword().trim() + "%");
        }
        if (form.getStatus() != null && !form.getStatus().isBlank()) {
            qw.eq(JobLogEntity::getStatus, form.getStatus());
        }
        if (form.getJobId() != null) {
            qw.eq(JobLogEntity::getJobId, form.getJobId());
        }
        qw.orderByDesc(JobLogEntity::getStartTime);

        Page<JobLogEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        Page<JobLogEntity> result = mapper.selectPage(page, qw);
        List<JobLogListVO> vos = result.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
    }

    public List<JobLogListVO> running() {
        LambdaQueryWrapper<JobLogEntity> qw = new LambdaQueryWrapper<JobLogEntity>()
                .eq(JobLogEntity::getStatus, "RUNNING")
                .orderByDesc(JobLogEntity::getStartTime);
        List<JobLogEntity> list = mapper.selectList(qw);
        return list.stream().map(converter::toListVO).toList();
    }
}
