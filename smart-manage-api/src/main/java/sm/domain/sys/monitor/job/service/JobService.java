package sm.domain.sys.monitor.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.job.model.entity.JobEntity;
import sm.domain.sys.monitor.job.model.entity.JobLogEntity;
import sm.domain.sys.monitor.job.model.form.JobListForm;
import sm.domain.sys.monitor.job.model.form.JobSaveForm;
import sm.domain.sys.monitor.job.model.vo.JobDetailVO;
import sm.domain.sys.monitor.job.model.vo.JobListVO;
import sm.domain.sys.monitor.job.mapper.JobLogMapper;
import sm.domain.sys.monitor.job.mapper.JobMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.domain.sys.base.common.helper.UserHelper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;
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

    private static final String MANAGED_JOB_ID_KEY = "smartManageJobId";

    private final JobMapper mapper;
    private final JobLogMapper jobLogMapper;
    private final Scheduler scheduler;
    private final ApplicationContext applicationContext;
    private final JobTxService txService;
    private final JsonMapper jsonMapper;
    private final JobConverter converter;

    // ==================== 查询 ====================

    public PageData<JobListVO> listPage(JobListForm form) {
        LambdaQueryWrapper<JobEntity> qw = new LambdaQueryWrapper<JobEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String kw = "%" + form.getKeyword().trim() + "%";
            qw.and(condition -> condition.like(JobEntity::getJobName, kw).or().like(JobEntity::getJobGroup, kw).or().like(JobEntity::getNumber, kw));
        }
        if (form.getStatus() != null && !form.getStatus().isBlank()) {
            qw.eq(JobEntity::getStatus, form.getStatus());
        }
        qw.orderByDesc(JobEntity::getCreateTime);

        Page<JobEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        Page<JobEntity> result = mapper.selectPage(page, qw);
        List<JobListVO> vos = result.getRecords().stream().map(this::assembleListVO).collect(Collectors.toList());
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
        UserHelper.checkAdmin();
        JobEntity previous = form.getId() == null ? null : mapper.selectById(form.getId());
        Long id = txService.save(form);
        JobEntity current = requireEntity(id);
        if (previous != null && (!previous.getJobName().equals(current.getJobName())
                || !previous.getJobGroup().equals(current.getJobGroup()))) {
            removeQuartzJob(previous.getJobName(), previous.getJobGroup());
        }
        synchronize(current);
        return id;
    }

    @BizLog("删除定时任务")
    public void deleteById(Long id) {
        UserHelper.checkAdmin();
        JobEntity entity = requireEntity(id);
        txService.deleteById(id);
        removeQuartzJob(entity.getJobName(), entity.getJobGroup());
    }

    // ==================== 任务操作 ====================

    @BizLog("暂停定时任务")
    public void pause(Long id) {
        UserHelper.checkAdmin();
        txService.pause(id);
        synchronize(requireEntity(id));
    }

    @BizLog("恢复定时任务")
    public void resume(Long id) {
        UserHelper.checkAdmin();
        txService.resume(id);
        synchronize(requireEntity(id));
    }

    /**
     * 以数据库为权威来源重新同步全部任务，并清理带本系统标识的 Quartz 孤儿任务。
     * 该入口用于 Quartz 临时故障恢复，可安全重复执行。
     */
    @BizLog("重新同步定时任务")
    public void syncAll() {
        UserHelper.checkAdmin();
        List<JobEntity> entities = mapper.selectList(new LambdaQueryWrapper<>());
        Set<Long> validIds = new HashSet<>();
        for (JobEntity entity : entities) {
            validIds.add(entity.getId());
            synchronize(entity);
        }
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                String managedId = jobDetail == null ? null : jobDetail.getJobDataMap().getString(MANAGED_JOB_ID_KEY);
                if (managedId != null && !validIds.contains(Long.valueOf(managedId))) {
                    scheduler.deleteJob(jobKey);
                }
            }
        } catch (SchedulerException | NumberFormatException exception) {
            log.error("Quartz 全量同步失败", exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 全量同步失败");
        }
    }

    @BizLog("立即执行定时任务")
    public void trigger(Long id) {
        UserHelper.checkAdmin();
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
        Map<String, Job> beans = applicationContext.getBeansOfType(Job.class);
        return beans.values().stream()
                .map(job -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("className", job.getClass().getName());
                    item.put("simpleName", job.getClass().getSimpleName());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 新建任务时的默认值
     */
    public Map<String, Object> createNewData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobGroup", "DEFAULT");
        data.put("status", "ENABLED");
        data.put("cronExpression", "0 0 3 * * ?");
        return data;
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
        Class<? extends Job> jobClass = resolveJobClass(entity.getJobClassName());
        JobDataMap dataMap = parseJobData(entity.getJobData());
        dataMap.put(MANAGED_JOB_ID_KEY, entity.getId().toString());
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(entity.getJobName(), entity.getJobGroup())
                .withDescription(entity.getRemark())
                .usingJobData(dataMap)
                .storeDurably()
                .build();
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(entity.getJobName() + "_trigger", entity.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.addJob(jobDetail, true);
                scheduler.rescheduleJob(trigger.getKey(), trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            if ("PAUSED".equals(entity.getStatus())) {
                scheduler.pauseJob(jobDetail.getKey());
            } else {
                scheduler.resumeJob(jobDetail.getKey());
            }
        } catch (SchedulerException exception) {
            log.error("Quartz 任务同步失败: id={}, group={}, name={}", entity.getId(), entity.getJobGroup(), entity.getJobName(), exception);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Quartz 任务同步失败，可执行重新同步恢复");
        }
    }

    private Class<? extends Job> resolveJobClass(String className) {
        try {
            Class<?> jobClass = Class.forName(className);
            if (!Job.class.isAssignableFrom(jobClass)) {
                throw new BizException(ResultEnum.CONFIG_ERROR, "任务类必须实现 org.quartz.Job 接口: " + className);
            }
            return jobClass.asSubclass(Job.class);
        } catch (ClassNotFoundException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "找不到任务类: " + className);
        }
    }

    private JobDataMap parseJobData(String jobDataJson) {
        JobDataMap dataMap = new JobDataMap();
        if (jobDataJson == null || jobDataJson.isBlank()) {
            return dataMap;
        }
        try {
            Map<String, Object> values = jsonMapper.readValue(jobDataJson, new TypeReference<>() {
            });
            values.forEach(dataMap::put);
            return dataMap;
        } catch (Exception exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务参数不是合法 JSON");
        }
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

    private JobListVO assembleListVO(JobEntity entity) {
        JobListVO vo = converter.toListVO(entity);
        JobLogEntity lastLog = getLastLog(entity.getId());
        if (lastLog != null) {
            vo.setLastExecuteTime(lastLog.getStartTime());
            vo.setLastExecuteStatus(lastLog.getStatus());
        }
        return vo;
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
