package sm.domain.sys.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.model.vo.JobExecutionSnapshotVO;
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
            // 只使用受管任务的持久化 ID 关联定义，不能从可修改名称或技术分组反查。
            String managedId = context.getJobDetail().getJobDataMap().getString(ManagedJobIdentity.JOB_ID_KEY);
            if (managedId == null) {
                throw new IllegalStateException("受管任务缺少任务 ID，拒绝执行");
            }
            JobExecutionSnapshotVO snapshot = jobMapper.selectExecutionSnapshot(Long.valueOf(managedId));
            if (snapshot == null) {
                throw new IllegalStateException("任务或所属应用已不存在，拒绝执行");
            }
            JobLogEntity logEntity = new JobLogEntity();
            logEntity.setJobId(snapshot.getJobId());
            logEntity.setJobName(snapshot.getJobName());
            logEntity.setDomainId(snapshot.getDomainId());
            logEntity.setDomainName(snapshot.getDomainName());
            logEntity.setAppId(snapshot.getAppId());
            logEntity.setAppName(snapshot.getAppName());
            logEntity.setStartTime(LocalDateTime.now());
            logEntity.setStatus(JobExecutionStatus.RUNNING.name());
            logEntity.setTraceId(TraceIdUtil.getTraceId());
            logEntity.setInstanceId(resolveInstanceId(context));
            logEntity.setFireInstanceId(context.getFireInstanceId());
            logEntity.setCreateTime(LocalDateTime.now());
            if (jobLogMapper.insert(logEntity) != 1) {
                throw new IllegalStateException("任务 RUNNING 执行记录写入失败，拒绝执行任务");
            }

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
            if (jobLogMapper.updateById(logEntity) != 1) {
                markUnknown(logEntity, "任务结果更新未命中执行记录");
            }
        } catch (RuntimeException exception) {
            Long logId = (Long) context.get("__jobLogId__");
            log.error("任务执行结果落库失败，执行状态需要对账: logId={}", logId, exception);
            if (logId != null) {
                JobLogEntity unknown = new JobLogEntity();
                unknown.setId(logId);
                markUnknown(unknown, "任务结果落库失败: " + truncate(exception.getMessage(), 1800));
            }
        } finally {
            TraceIdUtil.clear();
        }
    }

    private void markUnknown(JobLogEntity logEntity, String reason) {
        logEntity.setStatus(JobExecutionStatus.UNKNOWN.name());
        logEntity.setErrorMessage(reason);
        try {
            jobLogMapper.updateById(logEntity);
        } catch (RuntimeException retryException) {
            log.error("任务执行记录标记 UNKNOWN 失败，必须由监控对账: logId={}", logEntity.getId(), retryException);
        }
    }

    private boolean isMutexBusy(JobExecutionException exception) {
        return exception != null && exception.getCause() instanceof JobMutexBusyException;
    }

    private String resolveInstanceId(JobExecutionContext context) {
        try {
            return context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException exception) {
            throw new IllegalStateException("无法读取 Quartz 实例 ID", exception);
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
