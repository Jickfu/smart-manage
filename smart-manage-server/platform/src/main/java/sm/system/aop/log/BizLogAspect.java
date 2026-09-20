package sm.system.aop.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sm.system.security.context.CurrentOperatorProvider;
import sm.system.util.ServletUtil;
import sm.system.util.TraceIdUtil;
import sm.system.web.ClientIpResolver;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;

/**
 * 业务操作日志 AOP
 *
 * @author Chekfu
 */
@Aspect
@Order(100)
@Component
@Slf4j
@RequiredArgsConstructor
public class BizLogAspect {
    private final JsonMapper jsonMapper;
    private final OperateLogWriter operateLogWriter;
    private final CurrentOperatorProvider currentOperatorProvider;
    private final ClientIpResolver clientIpResolver;

    @Pointcut("@annotation(sm.system.aop.log.BizLog)")
    private void getLogPointCut() {
    }

    @Around("getLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        BizLog bizLog = method.getAnnotation(BizLog.class);
        long t0 = System.currentTimeMillis();

        String ip = null;
        String userAgent = null;
        String requestUri = null;
        String requestMethod = null;
        try {
            var req = ServletUtil.getRequest();
            ip = clientIpResolver.resolve(req);
            userAgent = req.getHeader("User-Agent");
            requestUri = req.getRequestURI();
            requestMethod = req.getMethod();
        } catch (Exception e) {
            log.warn("获取请求元数据失败", e);
        }

        Long userId = currentOperatorProvider.getCurrentUserIdOrNull();
        String username = currentOperatorProvider.getCurrentUsernameOrDefault("未知");

        String params = null;
        if (bizLog.recordRequest()) {
            params = LogPayloadUtil.truncate(
                    LogPayloadUtil.redactJson(serializeArgs(joinPoint.getArgs()), jsonMapper), bizLog.maxParamLen());
        }

        String err = null;
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            err = e.getMessage() != null ? LogPayloadUtil.truncate(e.getMessage(), 2000) : e.getClass().getSimpleName();
            long ms = System.currentTimeMillis() - t0;
            writeLog(bizLog, joinPoint, false, err, ip, userAgent, requestUri, requestMethod, userId, username, params, null, ms);
            throw e;
        }
        long duration = System.currentTimeMillis() - t0;
        String body = null;
        if (bizLog.recordResponse()) {
            body = LogPayloadUtil.truncate(
                    LogPayloadUtil.redactJson(serializeObject(result), jsonMapper), bizLog.maxResponseLen());
        }
        writeLog(bizLog, joinPoint, true, null, ip, userAgent, requestUri, requestMethod, userId, username, params, body, duration);
        return result;
    }

    private void writeLog(BizLog bizLog, JoinPoint joinPoint, boolean success, String errorMsg,
                          String ip, String userAgent, String requestUri, String requestMethod,
                          Long userId, String username, String requestParams, String responseBody, long durationMs) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        OperateLogPayload payload = new OperateLogPayload(
            bizLog.value(),
            success,
            errorMsg,
            requestMethod,
            requestUri,
            ip,
            userAgent,
            sig.getDeclaringTypeName(),
            sig.getName(),
            durationMs,
            requestParams,
            responseBody,
            userId,
            username,
            TraceIdUtil.getTraceId()
        );
        operateLogWriter.write(payload);
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return jsonMapper.writeValueAsString(args);
        } catch (JacksonException ex) {
            return "[unserializable args]";
        }
    }

    private String serializeObject(Object o) {
        if (o == null) {
            return "null";
        }
        try {
            return jsonMapper.writeValueAsString(o);
        } catch (JacksonException e) {
            return "[unserializable response]";
        }
    }
}
