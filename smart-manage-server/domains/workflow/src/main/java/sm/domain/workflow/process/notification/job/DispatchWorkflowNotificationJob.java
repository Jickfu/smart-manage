package sm.domain.workflow.process.notification.job;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.contract.SchedulerJobDefinition;
import sm.domain.workflow.process.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
@SchedulerJobDefinition(appNumber = "process", description = "重试投递已提交的工作流待办和结果通知")
public class DispatchWorkflowNotificationJob extends QuartzJobBean {
    private final NotificationService service;
    @Override
    protected void executeInternal(JobExecutionContext context) { service.dispatch(); }
}
