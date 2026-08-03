package sm.domain.sys.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 受管任务统一调度入口。互斥键按共享资源定义，因此不同任务也可以互斥。
 * 当前模块只支持单节点部署；升级 Quartz 集群前需将此锁替换为分布式锁。
 */
@Component
@RequiredArgsConstructor
public class ManagedJobDispatcher implements Job {
    static final String TARGET_CLASS_KEY = "__smartManageTargetClass__";
    static final String MUTEX_KEY = "__smartManageMutexKey__";

    private final ApplicationContext applicationContext;
    private final ConcurrentHashMap<String, AtomicBoolean> mutexes = new ConcurrentHashMap<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String targetClassName = context.getMergedJobDataMap().getString(TARGET_CLASS_KEY);
        String mutexKey = context.getMergedJobDataMap().getString(MUTEX_KEY);
        AtomicBoolean mutex = acquireMutex(mutexKey);
        try {
            Job target = applicationContext.getBeansOfType(Job.class).values().stream()
                    .filter(job -> AopUtils.getTargetClass(job).getName().equals(targetClassName))
                    .findFirst()
                    .orElseThrow(() -> new JobExecutionException("任务执行类未注册: " + targetClassName));
            target.execute(context);
        } finally {
            releaseMutex(mutexKey, mutex);
        }
    }

    private AtomicBoolean acquireMutex(String mutexKey) throws JobExecutionException {
        if (mutexKey == null || mutexKey.isBlank()) {
            return null;
        }
        AtomicBoolean mutex = mutexes.computeIfAbsent(mutexKey, ignored -> new AtomicBoolean());
        if (!mutex.compareAndSet(false, true)) {
            throw new JobExecutionException(new JobMutexBusyException(mutexKey), false);
        }
        return mutex;
    }

    private void releaseMutex(String mutexKey, AtomicBoolean mutex) {
        if (mutex == null) {
            return;
        }
        mutex.set(false);
    }
}
