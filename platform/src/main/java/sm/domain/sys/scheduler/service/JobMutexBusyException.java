package sm.domain.sys.scheduler.service;

/** 共享资源互斥键已被占用；该次触发应记录为跳过，而不是失败。 */
public class JobMutexBusyException extends RuntimeException {
    public JobMutexBusyException(String mutexKey) {
        super("共享资源正在被其他任务使用，已跳过本次执行: " + mutexKey);
    }
}
