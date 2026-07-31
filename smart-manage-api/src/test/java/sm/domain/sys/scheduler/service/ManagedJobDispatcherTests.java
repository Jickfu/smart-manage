package sm.domain.sys.scheduler.service;

import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagedJobDispatcherTests {

    @Test
    void sameResourceMutexSkipsSecondTaskEvenWithDifferentJobKeys() throws Exception {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger executions = new AtomicInteger();
        Job target = context -> {
            executions.incrementAndGet();
            started.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new JobExecutionException(exception);
            }
        };
        when(applicationContext.getBeansOfType(Job.class)).thenReturn(Map.of("target", target));
        ManagedJobDispatcher dispatcher = new ManagedJobDispatcher(applicationContext);
        JobExecutionContext first = context(target.getClass().getName(), "shared-storage");
        JobExecutionContext second = context(target.getClass().getName(), "shared-storage");

        Thread firstExecution = new Thread(() -> {
            try {
                dispatcher.execute(first);
            } catch (JobExecutionException exception) {
                throw new AssertionError(exception);
            }
        });
        firstExecution.start();
        assertTrue(started.await(5, TimeUnit.SECONDS));

        JobExecutionException exception =
                assertThrows(JobExecutionException.class, () -> dispatcher.execute(second));
        assertInstanceOf(JobMutexBusyException.class, exception.getCause());
        assertEquals(1, executions.get());

        release.countDown();
        firstExecution.join(5000);
    }

    private static JobExecutionContext context(String targetClassName, String mutexKey) {
        JobExecutionContext context = mock(JobExecutionContext.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ManagedJobDispatcher.TARGET_CLASS_KEY, targetClassName);
        dataMap.put(ManagedJobDispatcher.MUTEX_KEY, mutexKey);
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
        return context;
    }
}
