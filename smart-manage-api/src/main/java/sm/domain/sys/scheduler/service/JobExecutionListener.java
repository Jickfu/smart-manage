package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.constant.JobExecutionStatus;
import sm.system.util.TraceIdUtil;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quartz 全局 Job 监听器：记录每次执行到 t_sys_job_log
 *
 * @author Chekfu
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobExecutionListener implements JobListener {

    private final JobLogMapper jobLogMapper;
    private final JobMapper jobMapper;

    @Override
    public String getName() {
        return "JobExecutionListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        TraceIdUtil.setTraceId("job-" + UUID.randomUUID());
        try {
            String jobName = context.getJobDetail().getKey().getName();
            String jobGroup = context.getJobDetail().getKey().getGroup();

            JobEntity jobEntity = jobMapper.selectOne(
                    new LambdaQueryWrapper<JobEntity>()
                            .eq(JobEntity::getJobName, jobName)
                            .eq(JobEntity::getJobGroup, jobGroup));

            JobLogEntity logEntity = new JobLogEntity();
            logEntity.setJobId(jobEntity != null ? jobEntity.getId() : null);
            logEntity.setJobName(jobName);
            logEntity.setJobGroup(jobGroup);
            logEntity.setStartTime(LocalDateTime.now());
            logEntity.setStatus(JobExecutionStatus.RUNNING.name());
            logEntity.setTraceId(TraceIdUtil.getTraceId());
            logEntity.setCreateTime(LocalDateTime.now());
            jobLogMapper.insert(logEntity);

            context.put("__jobLogId__", logEntity.getId());
        } catch (RuntimeException exception) {
            TraceIdUtil.clear();
            throw exception;
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        TraceIdUtil.clear();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            Long logId = (Long) context.get("__jobLogId__");
            if (logId == null) {
                return;
            }
            JobLogEntity logEntity = jobLogMapper.selectById(logId);
            if (logEntity == null) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            logEntity.setEndTime(now);
            logEntity.setDurationMs(java.time.Duration.between(logEntity.getStartTime(), now).toMillis());
            if (isMutexBusy(jobException)) {
                logEntity.setStatus(JobExecutionStatus.SKIPPED.name());
                logEntity.setErrorMessage(truncate(jobException.getCause().getMessage(), 2000));
            } else if (jobException != null) {
                logEntity.setStatus(JobExecutionStatus.FAILED.name());
                logEntity.setErrorMessage(truncate(jobException.getMessage(), 2000));
            } else {
                logEntity.setStatus(JobExecutionStatus.SUCCESS.name());
            }
            jobLogMapper.updateById(logEntity);
        } finally {
            TraceIdUtil.clear();
        }
    }

    private boolean isMutexBusy(JobExecutionException exception) {
        return exception != null && exception.getCause() instanceof JobMutexBusyException;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
