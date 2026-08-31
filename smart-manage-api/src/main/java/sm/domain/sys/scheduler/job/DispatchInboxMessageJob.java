package sm.domain.sys.scheduler.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import sm.domain.sys.message.inbox.service.InboxMessageAdminService;

@Component
@Slf4j
@RequiredArgsConstructor
@SchedulerJobDefinition(description = "生成全站站内消息的用户收件快照", parameterTemplate = "{\"batchSize\":5}")
public class DispatchInboxMessageJob extends QuartzJobBean {
    private final InboxMessageAdminService service;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        int batchSize = 5;
        try {
            Object rawBatchSize = context.getMergedJobDataMap().get("batchSize");
            if (rawBatchSize != null) {
                batchSize = rawBatchSize instanceof Number number
                        ? number.intValue() : Integer.parseInt(rawBatchSize.toString());
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("站内消息派发任务参数必须是包含 batchSize 的 JSON 对象", exception);
        }
        int claimed = service.dispatchPending(batchSize);
        if (claimed > 0) {
            log.info("站内消息派发完成: claimed={}", claimed);
        } else {
            log.debug("站内消息派发完成: claimed=0");
        }
    }
}
