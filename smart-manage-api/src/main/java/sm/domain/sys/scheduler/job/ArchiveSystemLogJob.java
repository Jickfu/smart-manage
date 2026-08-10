package sm.domain.sys.scheduler.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

/** 将超过在线保留期的完整月日志分区转入历史父表。 */
@Component
@Slf4j
@RequiredArgsConstructor
@SchedulerJobDefinition(
        description = "将系统日志的到期完整月分区转入历史父表",
        parameterTemplate = "{\"loginLogHotDays\":180,\"operateLogHotDays\":180,\"sqlLogHotDays\":180,"
                + "\"scriptLogHotDays\":180,\"jobLogHotDays\":90,\"maxPartitionsPerRun\":12}")
public class ArchiveSystemLogJob extends QuartzJobBean {
    private final SystemLogPartitionLifecycleManager lifecycleManager;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        SystemLogLifecycleSettings settings = SystemLogLifecycleSettings.archive(context.getMergedJobDataMap());
        int archived = lifecycleManager.archive(settings);
        List<String> insufficientPartitions = lifecycleManager.partitionsRequiringExtension();
        log.info("系统日志分区转储完成: archived={}, insufficientPartitions={}", archived, insufficientPartitions);
        if (!insufficientPartitions.isEmpty()) {
            throw new JobExecutionException("未来日志分区不足六个月，请通过 Flyway 扩展: " + insufficientPartitions);
        }
    }
}
