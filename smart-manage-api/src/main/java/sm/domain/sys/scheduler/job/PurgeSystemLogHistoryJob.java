package sm.domain.sys.scheduler.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/** 删除超过历史保留期的完整月日志分区。 */
@Component
@Slf4j
@RequiredArgsConstructor
@SchedulerJobDefinition(
        description = "删除超过历史保留期的系统日志完整月分区",
        parameterTemplate = "{\"loginLogRetentionDays\":1095,\"operateLogRetentionDays\":1095,"
                + "\"sqlLogRetentionDays\":730,\"scriptLogRetentionDays\":730,"
                + "\"jobLogRetentionDays\":365,\"maxPartitionsPerRun\":12}")
public class PurgeSystemLogHistoryJob extends QuartzJobBean {
    private final SystemLogPartitionLifecycleManager lifecycleManager;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        SystemLogLifecycleSettings settings = SystemLogLifecycleSettings.purge(context.getMergedJobDataMap());
        int purged = lifecycleManager.purge(settings);
        log.info("系统日志历史分区淘汰完成: purged={}", purged);
    }
}
