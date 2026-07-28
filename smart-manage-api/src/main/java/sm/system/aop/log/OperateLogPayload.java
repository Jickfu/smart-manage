package sm.system.aop.log;

/**
 * 操作日志写入载荷 — 解耦 sm.system 与 sm.domain 的依赖
 *
 * @author Chekfu
 */
public record OperateLogPayload(
    String bizName,
    boolean success,
    String errorMsg,
    String requestMethod,
    String requestUri,
    String ip,
    String userAgent,
    String className,
    String methodName,
    long durationMs,
    String requestParams,
    String responseBody,
    Long userId,
    String username,
    String traceId
) {}
