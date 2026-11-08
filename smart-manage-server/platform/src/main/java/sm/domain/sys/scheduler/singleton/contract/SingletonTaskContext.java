package sm.domain.sys.scheduler.singleton.contract;

/** 单次执行上下文；异步回调必须捕获本次上下文，不得借用下一次运行的身份。 */
public interface SingletonTaskContext {
    String runId();
    long generation();
    boolean cancelled();
    void checkActive();
    /** 初始化完成后显式报告就绪，获得锁本身不表示业务已经可用。 */
    void ready();
    /** 注册快速、幂等的取消信号（例如关闭 socket）；不能等待工作线程自身结束。 */
    void onCancel(Runnable action);
    /** 在调用方已有事务内锁定并验证运行代际，保护同一事务内的业务数据库写入。 */
    void guardTransaction();
}
