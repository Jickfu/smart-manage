package sm.domain.sys.scheduler.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import sm.domain.sys.message.email.service.EmailService;

@Component
@Slf4j
@RequiredArgsConstructor
@SchedulerJobDefinition(description = "处理持久化邮件任务并执行有限重试", parameterTemplate = "{\"batchSize\":20}")
public class DispatchEmailJob extends QuartzJobBean {
    private final EmailService emailService;
    @Override protected void executeInternal(JobExecutionContext context) {
        int batchSize = 20;
        try {
            Object rawBatchSize = context.getMergedJobDataMap().get("batchSize");
            if (rawBatchSize != null) {
                batchSize = rawBatchSize instanceof Number number
                        ? number.intValue() : Integer.parseInt(rawBatchSize.toString());
            }
        } catch (Exception exception) { throw new IllegalArgumentException("邮件派发任务参数必须是包含 batchSize 的 JSON 对象", exception); }
        int claimed=emailService.dispatchPending(batchSize);
        log.info("邮件派发完成: claimed={}",claimed);
    }
}
