package sm.domain.sys.scheduler.singleton.contract;

/** 集群单例任务；每次接管重新调用 run，返回表示本次运行已经完成资源收束。 */
public interface SingletonTask {
    SingletonTaskDefinition definition();
    void run(SingletonTaskContext context) throws Exception;
    /** 管理端启停前的业务校验；不得在此产生外部副作用。 */
    default void validateControl(boolean requested) { }
}
