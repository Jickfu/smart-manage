package sm.domain.sys.scheduler.singleton.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.singleton.contract.SingletonTask;
import sm.system.concurrent.PostgresSessionFactory;
import sm.system.util.TraceIdUtil;

/** 每任务独立协调与工作线程；其他任务的阻塞不会饿死本任务的所有权检查。 */
@Component
public class SingletonTaskCoordinator implements AutoCloseable {
    @FunctionalInterface public interface ConnectionFactory { Connection open() throws Exception; }
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SingletonTaskCoordinator.class);
    private final SingletonTaskRegistry registry;
    private final SingletonTaskAccessor accessor;
    private final ConnectionFactory connections;
    private final String instanceId;
    private final List<Thread> supervisors = new ArrayList<>();
    private volatile boolean stopping;
    private boolean started;

    @Autowired
    public SingletonTaskCoordinator(SingletonTaskRegistry registry, SingletonTaskAccessor accessor,
            PostgresSessionFactory connections,
            @Value("${smart-manage.system.runtime.instance-id}") String instanceId) {
        this(registry, accessor, connections::open, instanceId);
    }
    public SingletonTaskCoordinator(SingletonTaskRegistry registry, SingletonTaskAccessor accessor,
            ConnectionFactory connections, String instanceId) {
        this.registry = registry;
        this.accessor = accessor;
        this.connections = connections;
        if (instanceId == null || instanceId.isBlank() || instanceId.length() > 120) {
            throw new IllegalArgumentException("单例任务要求有效的实例身份");
        }
        this.instanceId = instanceId;
    }
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (started) return;
        if (registry.tasks().size() > 32) throw new IllegalStateException("单例任务超出每实例32项连接预算");
        for (SingletonTask task : registry.tasks()) accessor.register(task.definition());
        started = true;
        for (SingletonTask task : registry.tasks()) {
            supervisors.add(Thread.ofPlatform().daemon().name("singleton-control-" + task.definition().number())
                    .start(() -> supervise(task)));
        }
    }
    private void supervise(SingletonTask task) {
        String number = task.definition().number();
        while (!stopping) {
            try {
                SingletonLease lease = SingletonLease.acquire(connections.open(), number, instanceId);
                if (lease != null) {
                    try (lease) {
                        String runId = UUID.randomUUID().toString();
                        if (lease.claim(runId)) execute(task, lease, runId);
                    }
                }
            } catch (Exception failure) {
                LOG.warn("单例任务协调失败，task={}，instance={}，category={}", number, instanceId,
                        failure.getClass().getSimpleName());
            }
            if (!stopping) pause();
        }
    }
    private void execute(SingletonTask task, SingletonLease lease, String runId) throws Exception {
        SingletonRunContext context = new SingletonRunContext(task.definition().number(), runId, lease.generation(), accessor);
        java.util.concurrent.atomic.AtomicReference<String> failureCategory = new java.util.concurrent.atomic.AtomicReference<>();
        Thread worker = Thread.ofPlatform().daemon().name("singleton-work-" + task.definition().number()).start(() -> {
            TraceIdUtil.setTraceId(runId);
            try {
                task.run(context);
                if (!context.cancelled()) failureCategory.set("UNEXPECTED_RETURN");
            } catch (Throwable failure) {
                if (!context.cancelled()) {
                    failureCategory.set("TASK_FAILED");
                    LOG.warn("单例任务执行失败，task={}，runId={}，category={}", task.definition().number(), runId,
                            failure.getClass().getSimpleName());
                }
            } finally { TraceIdUtil.clear(); }
        });
        boolean lost = false;
        try {
            while (true) {
                if (!worker.isAlive()) context.cancel();
                if (!worker.isAlive() && context.cancellationComplete()) break;
                if (stopping || context.cancelled()) { context.cancel(); worker.interrupt(); }
                if (!lost) {
                    try {
                        boolean requested = lease.heartbeat(context.cancelled() ? "STOPPING" : context.isReady() ? "RUNNING" : "STARTING");
                        if (!requested) { context.cancel(); worker.interrupt(); }
                        else if (!context.cancelled()) context.heartbeat();
                    } catch (Exception failure) {
                        lost = true;
                        context.cancel();
                        worker.interrupt();
                        LOG.warn("单例任务失去执行权，task={}，runId={}", task.definition().number(), runId);
                    }
                }
                pause();
            }
            if (!lost) lease.finish(failureCategory.get() != null, task.definition().retrySeconds(), failureCategory.get());
        } finally {
            context.cancel();
            worker.interrupt();
        }
    }
    private static void pause() {
        try { TimeUnit.SECONDS.sleep(2); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    }
    @Override @PreDestroy public void close() {
        stopping = true;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
        for (Thread supervisor : supervisors) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;
            try { supervisor.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(remaining))); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); break; }
        }
        if (supervisors.stream().anyMatch(Thread::isAlive)) {
            LOG.warn("单例任务尚未完成停止，保留执行权直到资源收束或JVM退出");
        }
    }
}
