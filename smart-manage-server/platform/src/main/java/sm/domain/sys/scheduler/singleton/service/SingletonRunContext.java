package sm.domain.sys.scheduler.singleton.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import sm.domain.sys.scheduler.singleton.contract.SingletonTaskContext;

/** 上下文只属于一次运行；单调时钟阻止停顿后的旧线程凭过期心跳继续派发工作。 */
final class SingletonRunContext implements SingletonTaskContext {
    private final String number;
    private final String runId;
    private final long generation;
    private final SingletonTaskAccessor accessor;
    private final List<Runnable> cancellations = new ArrayList<>();
    private volatile boolean cancelled;
    private volatile boolean ready;
    private volatile long validUntil = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    private volatile Thread cancellationThread;

    SingletonRunContext(String number, String runId, long generation, SingletonTaskAccessor accessor) {
        this.number = number;
        this.runId = runId;
        this.generation = generation;
        this.accessor = accessor;
    }
    @Override public String runId() { return runId; }
    @Override public long generation() { return generation; }
    @Override public boolean cancelled() {
        return cancelled || System.nanoTime() - validUntil >= 0;
    }
    @Override public void checkActive() {
        if (cancelled() || Thread.currentThread().isInterrupted()) throw new CancellationException("单例任务已取消");
    }
    @Override public void ready() { checkActive(); ready = true; }
    boolean isReady() { return ready; }
    void heartbeat() { validUntil = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); }
    @Override public synchronized void onCancel(Runnable action) {
        checkActive();
        cancellations.add(java.util.Objects.requireNonNull(action));
    }
    synchronized void cancel() {
        if (cancelled) return;
        cancelled = true;
        List<Runnable> actions = List.copyOf(cancellations);
        // 清理回调不能阻塞协调心跳；回调未完成时仍不释放正常持有的执行权。
        cancellationThread = Thread.ofVirtual().name("singleton-cancel-" + number).start(() -> {
            sm.system.util.TraceIdUtil.setTraceId(runId);
            try {
                for (Runnable action : actions) {
                    try { action.run(); }
                    catch (RuntimeException failure) {
                        org.slf4j.LoggerFactory.getLogger(SingletonRunContext.class)
                                .warn("单例任务取消回调失败，task={}，category={}", number, failure.getClass().getSimpleName());
                    }
                }
            } finally { sm.system.util.TraceIdUtil.clear(); }
        });
    }
    boolean cancellationComplete() { return cancellationThread == null || !cancellationThread.isAlive(); }
    @Override public void guardTransaction() {
        checkActive();
        accessor.guard(number, runId, generation);
    }
}
