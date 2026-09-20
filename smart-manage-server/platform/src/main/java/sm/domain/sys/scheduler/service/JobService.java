package sm.domain.sys.scheduler.service;

import sm.domain.sys.scheduler.converter.JobConverter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.SmartInitializingSingleton;
import sm.domain.sys.scheduler.constant.JobStatus;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.model.form.JobListForm;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.domain.sys.scheduler.model.form.JobCommandForm;
import sm.domain.sys.scheduler.model.vo.JobDetailVO;
import sm.domain.sys.scheduler.model.vo.JobListVO;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.query.ListSqlQuery;
import sm.domain.sys.base.app.service.AppReferenceService;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时任务管理 Service
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobService implements SmartInitializingSingleton {
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "number", ListSqlQuery.string("a.number", true),
            "jobName", ListSqlQuery.string("a.job_name", true),
            "appName", ListSqlQuery.string("b.name", false),
            "cronExpression", ListSqlQuery.string("a.cron_expression", false),
            "status", ListSqlQuery.enumeration("a.status", true),
            "jobClassName", ListSqlQuery.string("a.job_class_name", false));

    private final JobMapper mapper;
    private final JobLogMapper jobLogMapper;
    private final Scheduler scheduler;
    private final JobTxService txService;
    private final JobConverter converter;
    private final JobDefinitionValidator definitionValidator;
    private final AppReferenceService appReferenceService;

    // ==================== 查询 ====================

    public PageData<JobListVO> listPage(JobListForm form) {
        if (form.getStatus() != null && !form.getStatus().isBlank()) {
            JobStatus.require(form.getStatus());
        }
        Page<JobListVO> result = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()),
                form, ListSqlQuery.of(form, LIST_FIELDS));
        Map<Long, JobLogEntity> latestLogs = getLatestLogs(result.getRecords().stream().map(JobListVO::getId).toList());
        for (JobListVO item : result.getRecords()) {
            JobLogEntity lastLog = latestLogs.get(item.getId());
            if (lastLog != null) {
                item.setLastExecuteTime(lastLog.getStartTime());
                item.setLastExecuteStatus(lastLog.getStatus());
            }
        }
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
    }

    public JobDetailVO detail(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务ID不能为空");
        }
        JobEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
        }
        return assembleDetailVO(entity);
    }

    // ==================== 增删改 ====================

    @BizLog("保存定时任务")
    @AdministratorOnly
    public Long save(JobSaveForm form) {
        definitionValidator.validate(form);
        Long id = txService.save(form);
        JobEntity current = requireEntity(id);
        synchronize(current);
        return id;
    }

    @BizLog("删除定时任务")
    @AdministratorOnly
    public void deleteById(Long id, Integer version) {
        JobEntity entity = requireEntity(id);
        txService.deleteById(id, version);
        removeQuartzJob(entity.getId());
    }

    // ==================== 任务操作 ====================

    @BizLog("暂停定时任务")
    @AdministratorOnly
    public void pause(List<JobCommandForm> jobs) {
        txService.pause(jobs);
        jobs.forEach(job -> synchronize(requireEntity(job.getId())));
    }

    @BizLog("恢复定时任务")
    @AdministratorOnly
    public void resume(List<JobCommandForm> jobs) {
        txService.resume(jobs);
        jobs.forEach(job -> synchronize(requireEntity(job.getId())));
    }

    /**
     * 以数据库为权威来源重新同步全部任务，并清理带本系统标识的 Quartz 孤儿任务。
     * 该入口用于 Quartz 临时故障恢复，可安全重复执行。
     */
    @BizLog("重新同步定时任务")
    @AdministratorOnly
    public void syncAll() {
        List<JobEntity> entities = mapper.selectList(new LambdaQueryWrapper<>());
        synchronizeAll(entities, true);
    }

    /** 数据库定义是权威来源；启动时恢复全部任务，包括用户配置的暂停任务。 */
    @Override
    public void afterSingletonsInstantiated() {
        List<JobEntity> jobs = mapper.selectList(new LambdaQueryWrapper<>());
        synchronizeAll(jobs, true);
        try {
            scheduler.start();
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz 任务同步完成后无法启动调度器", exception);
        }
    }

    private void synchronizeAll(List<JobEntity> entities, boolean removeOrphans) {
        Map<Long, JobKey> expectedKeys = new java.util.HashMap<>();
        for (JobEntity entity : entities) {
            expectedKeys.put(entity.getId(), ManagedJobIdentity.jobKey(entity.getId()));
            synchronize(entity);
        }
        if (!removeOrphans) {
            return;
        }
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                String managedId = jobDetail == null ? null : jobDetail.getJobDataMap().getString(ManagedJobIdentity.JOB_ID_KEY);
                if (managedId != null) {
                    JobKey expectedKey = expectedKeys.get(Long.valueOf(managedId));
                    if (expectedKey == null || !expectedKey.equals(jobKey)) {
                        scheduler.deleteJob(jobKey);
                    }
                }
            }
        } catch (SchedulerException | NumberFormatException exception) {
            log.error("Quartz 全量同步失败", exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 全量同步失败");
        }
    }

    @BizLog("立即执行定时任务")
    @AdministratorOnly
    public void trigger(Long id) {
        JobEntity entity = requireEntity(id);
        try {
            JobKey jobKey = ManagedJobIdentity.jobKey(entity.getId());
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "触发任务失败");
        }
    }

    // ==================== 可选 Job 类列表 ====================

    /**
     * 获取所有可用的 Job 实现类（Spring 容器中所有 Job 类型的 Bean）
     */
    @AdministratorOnly
    public List<Map<String, String>> getAvailableJobClasses() {
        return definitionValidator.availableJobClasses();
    }

    /**
     * 新建任务时的默认值
     */
    @AdministratorOnly
    public Map<String, Object> createNewData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", JobStatus.PAUSED.name());
        data.put("cronExpression", "0 0 3 * * ?");
        return data;
    }

    public List<java.time.LocalDateTime> previewCron(String cronExpression) {
        if (cronExpression == null || !CronExpression.isValidExpression(cronExpression.trim())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Cron 表达式不合法");
        }
        try {
            CronExpression expression = new CronExpression(cronExpression.trim());
            java.util.Date cursor = new java.util.Date();
            java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
            java.util.ArrayList<java.time.LocalDateTime> result = new java.util.ArrayList<>(5);
            for (int index = 0; index < 5; index++) {
                cursor = expression.getNextValidTimeAfter(cursor);
                if (cursor == null) {
                    break;
                }
                result.add(cursor.toInstant().atZone(zoneId).toLocalDateTime());
            }
            return result;
        } catch (java.text.ParseException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Cron 表达式不合法");
        }
    }

    private JobEntity requireEntity(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务ID不能为空");
        }
        JobEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
        }
        return entity;
    }

    private void synchronize(JobEntity entity) {
        definitionValidator.resolveJobClass(entity.getJobClassName());
        JobDataMap dataMap = parseJobData(entity.getJobData());
        dataMap.put(ManagedJobIdentity.JOB_ID_KEY, entity.getId().toString());
        dataMap.put(ManagedJobDispatcher.TARGET_CLASS_KEY, entity.getJobClassName());
        if (entity.getMutexKey() != null && !entity.getMutexKey().isBlank()) {
            dataMap.put(ManagedJobDispatcher.MUTEX_KEY, entity.getMutexKey());
        }
        JobDetail jobDetail = JobBuilder.newJob(ManagedJobDispatcher.class)
                .withIdentity(ManagedJobIdentity.jobKey(entity.getId()))
                .withDescription(entity.getDescription())
                .usingJobData(dataMap)
                .storeDurably()
                .build();
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(ManagedJobIdentity.triggerKey(entity.getId()))
                .forJob(jobDetail.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.addJob(jobDetail, true);
                if (scheduler.checkExists(trigger.getKey())) {
                    scheduler.rescheduleJob(trigger.getKey(), trigger);
                } else {
                    scheduler.scheduleJob(trigger);
                }
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            if (JobStatus.PAUSED.name().equals(entity.getStatus())) {
                scheduler.pauseJob(jobDetail.getKey());
            } else {
                scheduler.resumeJob(jobDetail.getKey());
            }
        } catch (SchedulerException exception) {
            log.error("Quartz 任务同步失败: id={}, name={}", entity.getId(), entity.getJobName(), exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 任务同步失败，可执行重新同步恢复");
        }
    }

    private JobDataMap parseJobData(String jobDataJson) {
        JobDataMap dataMap = new JobDataMap();
        definitionValidator.parseJobData(jobDataJson).forEach(dataMap::put);
        return dataMap;
    }

    private void removeQuartzJob(Long jobId) {
        try {
            scheduler.deleteJob(ManagedJobIdentity.jobKey(jobId));
        } catch (SchedulerException exception) {
            log.error("Quartz 任务删除失败: id={}", jobId, exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 任务删除失败，可执行重新同步恢复");
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 获取某个任务的最后一次执行日志
     */
    private JobLogEntity getLastLog(Long jobId) {
        LambdaQueryWrapper<JobLogEntity> qw = new LambdaQueryWrapper<JobLogEntity>()
                .eq(JobLogEntity::getJobId, jobId)
                .orderByDesc(JobLogEntity::getCreateTime);
        Page<JobLogEntity> page = jobLogMapper.selectPage(new Page<>(1, 1, false), qw);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    private Map<Long, JobLogEntity> getLatestLogs(List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return Map.of();
        }
        return jobLogMapper.selectLatestByJobIds(jobIds).stream()
                .collect(Collectors.toMap(JobLogEntity::getJobId, logEntity -> logEntity));
    }

    private JobDetailVO assembleDetailVO(JobEntity entity) {
        JobDetailVO vo = converter.toDetailVO(entity);
        AppEntity app = appReferenceService.require(entity.getAppId());
        vo.setApp(new ReferenceVO(app.getId(), app.getNumber(), app.getName()));
        JobLogEntity lastLog = getLastLog(entity.getId());
        if (lastLog != null) {
            vo.setLastExecuteTime(lastLog.getStartTime());
            vo.setLastExecuteStatus(lastLog.getStatus());
        }
        try {
            TriggerKey triggerKey = ManagedJobIdentity.triggerKey(entity.getId());
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger != null && trigger.getNextFireTime() != null) {
                vo.setNextFireTime(trigger.getNextFireTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (SchedulerException e) {
            log.debug("获取下次触发时间失败: {}", entity.getJobName(), e);
        }
        return vo;
    }
}
