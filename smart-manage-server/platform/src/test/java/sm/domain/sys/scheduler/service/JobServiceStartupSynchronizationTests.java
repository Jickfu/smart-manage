package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import sm.domain.sys.base.app.service.AppReferenceService;
import sm.domain.sys.scheduler.converter.JobConverter;
import sm.domain.sys.scheduler.job.CleanTempFileJob;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobServiceStartupSynchronizationTests {
    @Test
    void startupRestoresAllDefinitionsAndOnlyRemovesManagedOrphans() throws Exception {
        JobMapper mapper = mock(JobMapper.class);
        Scheduler scheduler = mock(Scheduler.class);
        JobDefinitionValidator validator = mock(JobDefinitionValidator.class);
        JobEntity systemJob = task(1L, "系统任务", "ENABLED", true);
        JobEntity userJob = task(2L, "用户任务", "PAUSED", false);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(systemJob, userJob));
        doReturn(CleanTempFileJob.class).when(validator).resolveJobClass(any());
        when(validator.parseJobData(any())).thenReturn(Map.of());
        JobKey orphanKey = ManagedJobIdentity.jobKey(3L);
        JobKey externalKey = JobKey.jobKey("external", "external");
        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenReturn(Set.of(orphanKey, externalKey));
        when(scheduler.getJobDetail(orphanKey)).thenReturn(JobBuilder.newJob(ManagedJobDispatcher.class)
                .withIdentity(orphanKey).usingJobData(ManagedJobIdentity.JOB_ID_KEY, "3").build());
        when(scheduler.getJobDetail(externalKey)).thenReturn(JobBuilder.newJob(CleanTempFileJob.class)
                .withIdentity(externalKey).build());
        JobService service = new JobService(mapper, mock(JobLogMapper.class), scheduler,
                mock(JobTxService.class), mock(JobConverter.class), validator, mock(AppReferenceService.class));

        service.afterSingletonsInstantiated();

        ArgumentCaptor<JobDetail> details = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggers = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler, times(2)).scheduleJob(details.capture(), triggers.capture());
        assertEquals(List.of(ManagedJobIdentity.jobKey(1L), ManagedJobIdentity.jobKey(2L)),
                details.getAllValues().stream().map(JobDetail::getKey).toList());
        assertEquals(List.of(ManagedJobIdentity.triggerKey(1L), ManagedJobIdentity.triggerKey(2L)),
                triggers.getAllValues().stream().map(Trigger::getKey).toList());
        verify(scheduler).resumeJob(ManagedJobIdentity.jobKey(1L));
        verify(scheduler).pauseJob(ManagedJobIdentity.jobKey(2L));
        verify(scheduler).deleteJob(orphanKey);
        verify(scheduler, never()).deleteJob(externalKey);
        verify(scheduler).start();

        // 改名称和业务应用后继续更新同一 Quartz 身份，不产生第二套调度项。
        userJob.setJobName("重命名任务");
        userJob.setAppId(30L);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.checkExists(any(TriggerKey.class))).thenReturn(true);
        service.afterSingletonsInstantiated();
        verify(scheduler).rescheduleJob(eq(ManagedJobIdentity.triggerKey(2L)), any());
        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(scheduler, times(2)).start();
    }

    @Test
    void startupDoesNotStartSchedulerWhenManagedTaskRepairFails() throws Exception {
        JobMapper mapper = mock(JobMapper.class);
        Scheduler scheduler = mock(Scheduler.class);
        JobDefinitionValidator validator = mock(JobDefinitionValidator.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(task(1L, "系统任务", "ENABLED", true)));
        doReturn(CleanTempFileJob.class).when(validator).resolveJobClass(any());
        when(validator.parseJobData(any())).thenReturn(Map.of());
        doThrow(new SchedulerException("broken trigger")).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
        JobService service = new JobService(mapper, mock(JobLogMapper.class), scheduler,
                mock(JobTxService.class), mock(JobConverter.class), validator, mock(AppReferenceService.class));

        org.junit.jupiter.api.Assertions.assertThrows(sm.system.exception.BizException.class,
                service::afterSingletonsInstantiated);

        verify(scheduler, never()).start();
    }

    private static JobEntity task(Long id, String name, String status, boolean system) {
        JobEntity entity = new JobEntity();
        entity.setId(id);
        entity.setJobName(name);
        entity.setAppId(31L);
        entity.setJobClassName(CleanTempFileJob.class.getName());
        entity.setCronExpression("0 0/30 * * * ?");
        entity.setJobData("{}");
        entity.setStatus(status);
        entity.setIsSystem(system);
        return entity;
    }
}
