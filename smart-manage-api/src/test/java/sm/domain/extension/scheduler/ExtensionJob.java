package sm.domain.extension.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import sm.domain.sys.scheduler.contract.SchedulerJobDefinition;

/** 外部领域仅消费已发布调度元数据契约的测试任务。 */
@SchedulerJobDefinition(description = "扩展业务任务", parameterTemplate = "{\"batchSize\":10}")
public class ExtensionJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        // 元数据发现测试不执行任何业务副作用。
    }
}
