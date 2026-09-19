package sm.system.concurrent;

/** 基于共享基础设施的跨实例互斥能力。 */
public interface DistributedMutex {
    LockHandle acquire(String namespace, String key);

    interface LockHandle extends AutoCloseable {
        @Override
        void close();
    }
}
