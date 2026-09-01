package sm.domain.sys.scheduler.service;

import sm.domain.sys.scheduler.converter.JobConverter;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceStartupSynchronizationTests {

    @Test
    void startupSynchronizesEnabledSystemJobsWithoutRemovingUserJobs() throws Exception {
        JobMapper mapper = mock(JobMapper.class);
        Scheduler scheduler = mock(Scheduler.class);
        JobDefinitionValidator validator = mock(JobDefinitionValidator.class);
        JobEntity cleanupJob = new JobEntity();
        cleanupJob.setId(440000000000000003L);
        cleanupJob.setJobName("附件对象清理");
        cleanupJob.setJobGroup("SYSTEM");
        cleanupJob.setJobClassName("sm.domain.sys.scheduler.job.CleanTempFileJob");
        cleanupJob.setCronExpression("0 0/30 * * * ?");
        cleanupJob.setJobData("{}");
        cleanupJob.setStatus("ENABLED");
        cleanupJob.setMutexKey("attachment-object-cleanup");
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(cleanupJob));
        org.mockito.Mockito.doReturn(sm.domain.sys.scheduler.job.CleanTempFileJob.class)
                .when(validator).resolveJobClass(cleanupJob.getJobClassName());
        when(validator.parseJobData("{}")).thenReturn(java.util.Map.of());
        when(scheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(false);

        JobService service = new JobService(mapper, mock(JobLogMapper.class), scheduler,
                mock(JobTxService.class), mock(JobConverter.class), validator);

        service.synchronizeSystemJobsOnStartup();

        verify(scheduler).scheduleJob(any(org.quartz.JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, never()).getJobKeys(any(GroupMatcher.class));
    }
}
