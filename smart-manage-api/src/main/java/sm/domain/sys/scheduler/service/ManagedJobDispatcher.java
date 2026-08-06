package sm.domain.sys.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sm.system.concurrent.DistributedMutex;
import sm.system.concurrent.DistributedMutexBusyException;

/**
 * 受管任务统一调度入口。互斥键按共享资源定义，因此不同任务也可以跨实例互斥。
 */
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class ManagedJobDispatcher implements Job {
    static final String TARGET_CLASS_KEY = "__smartManageTargetClass__";
    static final String MUTEX_KEY = "__smartManageMutexKey__";

    private final ApplicationContext applicationContext;
    private final DistributedMutex distributedMutex;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String targetClassName = context.getMergedJobDataMap().getString(TARGET_CLASS_KEY);
        String mutexKey = context.getMergedJobDataMap().getString(MUTEX_KEY);
        try (DistributedMutex.LockHandle ignored = distributedMutex.acquire("scheduler", mutexKey)) {
            Job target = applicationContext.getBeansOfType(Job.class).values().stream()
                    .filter(job -> AopUtils.getTargetClass(job).getName().equals(targetClassName))
                    .findFirst()
                    .orElseThrow(() -> new JobExecutionException("任务执行类未注册: " + targetClassName));
            target.execute(context);
        } catch (DistributedMutexBusyException exception) {
            throw new JobExecutionException(new JobMutexBusyException(mutexKey), false);
        }
    }
}
