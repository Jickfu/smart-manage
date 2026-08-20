package sm.system.aop.log;

/**
 * 操作日志写入器接口 — 由业务领域模块实现，sm.system 仅依赖此接口
 *
 * @author Chekfu
 */
@FunctionalInterface
public interface OperateLogWriter {

    /**
     * 写入操作日志
     */
    void write(OperateLogPayload payload);
}
