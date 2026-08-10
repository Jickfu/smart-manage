package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.model.form.JobLogListForm;
import sm.domain.sys.scheduler.model.vo.JobLogListVO;
import sm.domain.sys.scheduler.model.vo.JobLogDetailVO;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.constant.JobExecutionStatus;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

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
        qw.select(JobLogEntity::getId, JobLogEntity::getJobId, JobLogEntity::getJobName,
                JobLogEntity::getJobGroup, JobLogEntity::getStartTime, JobLogEntity::getEndTime,
                JobLogEntity::getDurationMs, JobLogEntity::getStatus, JobLogEntity::getErrorMessage,
                JobLogEntity::getTraceId, JobLogEntity::getCreateTime);
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            // MyBatis-Plus 的 like 会添加两侧通配符；用户输入按模糊搜索语义处理。
            qw.like(JobLogEntity::getJobName, form.getKeyword().trim());
        }
        if (form.getStatus() != null && !form.getStatus().isBlank()) {
            JobExecutionStatus.require(form.getStatus());
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
                .eq(JobLogEntity::getStatus, JobExecutionStatus.RUNNING.name())
                .orderByDesc(JobLogEntity::getStartTime);
        List<JobLogEntity> list = mapper.selectList(qw);
        return list.stream().map(converter::toListVO).toList();
    }

    public JobLogDetailVO detail(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "执行实例ID不能为空");
        }
        JobLogEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "执行实例不存在");
        }
        return converter.toDetailVO(entity);
    }
}
