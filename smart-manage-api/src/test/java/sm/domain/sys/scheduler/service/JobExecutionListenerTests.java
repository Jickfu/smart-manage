package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import sm.domain.sys.scheduler.job.CleanTempFileJob;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.system.util.TraceIdUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobExecutionListenerTests {

	private final JobLogMapper jobLogMapper = mock(JobLogMapper.class);
	private final JobMapper jobMapper = mock(JobMapper.class);
	private final JobExecutionListener listener = new JobExecutionListener(jobLogMapper, jobMapper);

	@BeforeAll
	static void initializeMybatisMetadata() {
		TableInfoHelper.initTableInfo(
				new MapperBuilderAssistant(new MybatisConfiguration(), "job-listener-test"),
				JobEntity.class);
	}

	@AfterEach
	void clearTraceId() {
		TraceIdUtil.clear();
	}

	@Test
	void executionGetsIndependentTraceIdAndFailureIsRecorded() throws org.quartz.SchedulerException {
		JobExecutionContext context = mock(JobExecutionContext.class);
		when(context.getJobDetail()).thenReturn(JobBuilder.newJob(CleanTempFileJob.class)
				.withIdentity(JobKey.jobKey("clean-temp", "SYSTEM"))
				.build());
		when(context.get("__jobLogId__")).thenReturn(10L);
		Scheduler scheduler = mock(Scheduler.class);
		when(scheduler.getSchedulerInstanceId()).thenReturn("instance-a");
		when(context.getScheduler()).thenReturn(scheduler);
		when(context.getFireInstanceId()).thenReturn("fire-1");
		JobEntity job = new JobEntity();
		job.setId(1L);
		when(jobMapper.selectOne(any())).thenReturn(job);
		when(jobLogMapper.insert(any(JobLogEntity.class))).thenAnswer(invocation -> {
			JobLogEntity entity = invocation.getArgument(0);
			entity.setId(10L);
			return 1;
		});
		when(jobLogMapper.updateById(any(JobLogEntity.class))).thenReturn(1);

		listener.jobToBeExecuted(context);

		ArgumentCaptor<JobLogEntity> captor = ArgumentCaptor.forClass(JobLogEntity.class);
		verify(jobLogMapper).insert(captor.capture());
		JobLogEntity logEntity = captor.getValue();
		assertNotNull(logEntity.getTraceId());
		assertTrue(logEntity.getTraceId().startsWith("job-"));
		assertEquals(logEntity.getTraceId(), TraceIdUtil.getTraceId());
		when(jobLogMapper.selectById(10L)).thenReturn(logEntity);

		listener.jobWasExecuted(context, new JobExecutionException("execution failed"));

		assertEquals("FAILED", logEntity.getStatus());
		assertEquals("execution failed", logEntity.getErrorMessage());
		assertNull(TraceIdUtil.getTraceId());
		verify(jobLogMapper).updateById(logEntity);
	}
}
