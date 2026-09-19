package sm.domain.sys.scheduler.service;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/** 调度身份只依赖持久化任务 ID；业务名称和目录调整不得重建调度身份。 */
final class ManagedJobIdentity {
    static final String JOB_ID_KEY = "smartManageJobId";
    private static final String GROUP = "SMART_MANAGE";

    private ManagedJobIdentity() {
    }

    static JobKey jobKey(Long jobId) {
        return JobKey.jobKey("job:" + jobId, GROUP);
    }

    static TriggerKey triggerKey(Long jobId) {
        return TriggerKey.triggerKey("trigger:" + jobId, GROUP);
    }
}
