package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
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
import sm.system.security.context.CurrentUserContext;
import sm.system.query.ListQueryUtil;
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
public class JobService {
    private final CurrentUserContext currentUserContext;

    private static final String MANAGED_JOB_ID_KEY = "smartManageJobId";
    private static final Map<String, ListQueryUtil.Field<JobEntity>> LIST_FIELDS = Map.of(
            "number", ListQueryUtil.string(JobEntity::getNumber, true),
            "jobName", ListQueryUtil.string(JobEntity::getJobName, true),
            "jobGroup", ListQueryUtil.string(JobEntity::getJobGroup, true),
            "cronExpression", ListQueryUtil.string(JobEntity::getCronExpression, false),
            "status", ListQueryUtil.enumeration(JobEntity::getStatus, true),
            "jobClassName", ListQueryUtil.string(JobEntity::getJobClassName, false));

    private final JobMapper mapper;
    private final JobLogMapper jobLogMapper;
    private final Scheduler scheduler;
    private final JobTxService txService;
    private final JobConverter converter;
    private final JobDefinitionValidator definitionValidator;

    // ==================== 查询 ====================

    public PageData<JobListVO> listPage(JobListForm form) {
        LambdaQueryWrapper<JobEntity> qw = new LambdaQueryWrapper<JobEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String kw = "%" + form.getKeyword().trim() + "%";
            qw.and(condition -> condition.like(JobEntity::getJobName, kw).or().like(JobEntity::getJobGroup, kw).or().like(JobEntity::getNumber, kw));
        }
        if (form.getStatus() != null && !form.getStatus().isBlank()) {
            JobStatus.require(form.getStatus());
            qw.eq(JobEntity::getStatus, form.getStatus());
        }
        ListQueryUtil.apply(qw, form, LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) qw.orderByDesc(JobEntity::getCreateTime);
        if (!ListQueryUtil.isSortedBy(form, "id")) qw.orderByDesc(JobEntity::getId);

        Page<JobEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        Page<JobEntity> result = mapper.selectPage(page, qw);
        Map<Long, JobLogEntity> latestLogs = getLatestLogs(result.getRecords());
        List<JobListVO> vos = result.getRecords().stream()
                .map(entity -> assembleListVO(entity, latestLogs.get(entity.getId())))
                .collect(Collectors.toList());
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
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
    public Long save(JobSaveForm form) {
        currentUserContext.checkAdministrator();
        definitionValidator.validate(form);
        JobEntity previous = form.getId() == null ? null : mapper.selectById(form.getId());
        Long id = txService.save(form);
        JobEntity current = requireEntity(id);
        synchronize(current);
        if (previous != null && (!previous.getJobName().equals(current.getJobName())
                || !previous.getJobGroup().equals(current.getJobGroup()))) {
            removeQuartzJob(previous.getJobName(), previous.getJobGroup());
        }
        return id;
    }

    @BizLog("删除定时任务")
    public void deleteById(Long id, Integer version) {
        currentUserContext.checkAdministrator();
        JobEntity entity = requireEntity(id);
        txService.deleteById(id, version);
        removeQuartzJob(entity.getJobName(), entity.getJobGroup());
    }

    // ==================== 任务操作 ====================

    @BizLog("暂停定时任务")
    public void pause(List<JobCommandForm> jobs) {
        currentUserContext.checkAdministrator();
        txService.pause(jobs);
        jobs.forEach(job -> synchronize(requireEntity(job.getId())));
    }

    @BizLog("恢复定时任务")
    public void resume(List<JobCommandForm> jobs) {
        currentUserContext.checkAdministrator();
        txService.resume(jobs);
        jobs.forEach(job -> synchronize(requireEntity(job.getId())));
    }

    /**
     * 以数据库为权威来源重新同步全部任务，并清理带本系统标识的 Quartz 孤儿任务。
     * 该入口用于 Quartz 临时故障恢复，可安全重复执行。
     */
    @BizLog("重新同步定时任务")
    public void syncAll() {
        currentUserContext.checkAdministrator();
        List<JobEntity> entities = mapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, JobKey> expectedKeys = new java.util.HashMap<>();
        for (JobEntity entity : entities) {
            expectedKeys.put(entity.getId(), JobKey.jobKey(entity.getJobName(), entity.getJobGroup()));
            synchronize(entity);
        }
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                String managedId = jobDetail == null ? null : jobDetail.getJobDataMap().getString(MANAGED_JOB_ID_KEY);
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
    public void trigger(Long id) {
        currentUserContext.checkAdministrator();
        JobEntity entity = requireEntity(id);
        try {
            JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "触发任务失败");
        }
    }

    // ==================== 可选 Job 类列表 ====================

    /**
     * 获取所有可用的 Job 实现类（Spring 容器中所有 Job 类型的 Bean）
     */
    public List<Map<String, String>> getAvailableJobClasses() {
        currentUserContext.checkAdministrator();
        return definitionValidator.availableJobClasses();
    }

    /**
     * 新建任务时的默认值
     */
    public Map<String, Object> createNewData() {
        currentUserContext.checkAdministrator();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobGroup", "DEFAULT");
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
        dataMap.put(MANAGED_JOB_ID_KEY, entity.getId().toString());
        dataMap.put(ManagedJobDispatcher.TARGET_CLASS_KEY, entity.getJobClassName());
        if (entity.getMutexKey() != null && !entity.getMutexKey().isBlank()) {
            dataMap.put(ManagedJobDispatcher.MUTEX_KEY, entity.getMutexKey());
        }
        JobDetail jobDetail = JobBuilder.newJob(ManagedJobDispatcher.class)
                .withIdentity(entity.getJobName(), entity.getJobGroup())
                .withDescription(entity.getDescription())
                .usingJobData(dataMap)
                .storeDurably()
                .build();
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(entity.getJobName() + "_trigger", entity.getJobGroup())
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
            log.error("Quartz 任务同步失败: id={}, group={}, name={}", entity.getId(), entity.getJobGroup(), entity.getJobName(), exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 任务同步失败，可执行重新同步恢复");
        }
    }

    private JobDataMap parseJobData(String jobDataJson) {
        JobDataMap dataMap = new JobDataMap();
        definitionValidator.parseJobData(jobDataJson).forEach(dataMap::put);
        return dataMap;
    }

    private void removeQuartzJob(String jobName, String jobGroup) {
        try {
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException exception) {
            log.error("Quartz 任务删除失败: group={}, name={}", jobGroup, jobName, exception);
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

    private JobListVO assembleListVO(JobEntity entity, JobLogEntity lastLog) {
        JobListVO vo = converter.toListVO(entity);
        if (lastLog != null) {
            vo.setLastExecuteTime(lastLog.getStartTime());
            vo.setLastExecuteStatus(lastLog.getStatus());
        }
        return vo;
    }

    private Map<Long, JobLogEntity> getLatestLogs(List<JobEntity> entities) {
        if (entities.isEmpty()) {
            return Map.of();
        }
        List<Long> jobIds = entities.stream().map(JobEntity::getId).toList();
        return jobLogMapper.selectLatestByJobIds(jobIds).stream()
                .collect(Collectors.toMap(JobLogEntity::getJobId, logEntity -> logEntity));
    }

    private JobDetailVO assembleDetailVO(JobEntity entity) {
        JobDetailVO vo = converter.toDetailVO(entity);
        JobLogEntity lastLog = getLastLog(entity.getId());
        if (lastLog != null) {
            vo.setLastExecuteTime(lastLog.getStartTime());
            vo.setLastExecuteStatus(lastLog.getStatus());
        }
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(entity.getJobName() + "_trigger", entity.getJobGroup());
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
