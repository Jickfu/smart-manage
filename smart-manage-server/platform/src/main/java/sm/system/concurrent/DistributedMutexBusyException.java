package sm.system.concurrent;

/** 分布式互斥锁已被其他实例持有。 */
public class DistributedMutexBusyException extends RuntimeException {
    public DistributedMutexBusyException(String key) {
        super("资源正在由其他任务处理: " + key);
    }
}
