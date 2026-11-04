package sm.system.aop.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sm.system.security.context.CurrentOperatorProvider;
import sm.system.web.ClientIpResolver;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BizLogAspectTests {
    private final OperateLogWriter operateLogWriter = mock(OperateLogWriter.class);
    private final CurrentOperatorProvider currentOperatorProvider = mock(CurrentOperatorProvider.class);
    private final ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
    private BizLogAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new BizLogAspect(
                JsonMapper.builder().build(), operateLogWriter, currentOperatorProvider, clientIpResolver);
        when(currentOperatorProvider.getCurrentUserIdOrNull()).thenReturn(42L);
        when(currentOperatorProvider.getCurrentUsernameOrDefault("未知")).thenReturn("operator");
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void nonWebInvocationWritesLogWithoutRequestMetadataOrWarning() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPointReturning("completed");
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        Object result;
        try {
            result = aspect.around(joinPoint);
        } finally {
            detachLogAppender(appender);
        }

        assertEquals("completed", result);
        OperateLogPayload payload = capturedPayload();
        assertRequestMetadata(payload, null, null, null, null);
        assertEquals(42L, payload.userId());
        assertEquals("operator", payload.username());
        assertTrue(payload.success());
        assertTrue(appender.list.stream().noneMatch(event -> event.getLevel() == Level.WARN));
        verifyNoInteractions(clientIpResolver);
    }

    @Test
    void webInvocationWritesCompleteRequestMetadata() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/smart-manage-api/demo/run");
        request.addHeader("User-Agent", "SmartManage-Test/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(clientIpResolver.resolve(request)).thenReturn("203.0.113.8");

        Object result = aspect.around(joinPointReturning("completed"));

        assertEquals("completed", result);
        assertRequestMetadata(
                capturedPayload(), "POST", "/smart-manage-api/demo/run", "203.0.113.8", "SmartManage-Test/1.0");
    }

    @Test
    void requestMetadataFailureWarnsButStillExecutesBusinessMethod() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/smart-manage-api/demo/run");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(clientIpResolver.resolve(request)).thenThrow(new IllegalStateException("metadata unavailable"));
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        Object result;
        try {
            result = aspect.around(joinPointReturning("completed"));
        } finally {
            detachLogAppender(appender);
        }

        assertEquals("completed", result);
        OperateLogPayload payload = capturedPayload();
        assertTrue(payload.success());
        assertRequestMetadata(payload, null, null, null, null);
        assertTrue(appender.list.stream().anyMatch(event ->
                event.getLevel() == Level.WARN && "获取请求元数据失败".equals(event.getFormattedMessage())));
    }

    @Test
    void nonWebBusinessFailureWritesFailedLogAndRethrowsOriginalException() throws Throwable {
        IllegalArgumentException failure = new IllegalArgumentException("business failed");
        ProceedingJoinPoint joinPoint = joinPointThrowing(failure);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class, () -> aspect.around(joinPoint));

        assertSame(failure, thrown);
        OperateLogPayload payload = capturedPayload();
        assertFalse(payload.success());
        assertEquals("business failed", payload.errorMsg());
        assertRequestMetadata(payload, null, null, null, null);
        verifyNoInteractions(clientIpResolver);
    }

    private ProceedingJoinPoint joinPointReturning(Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    private ProceedingJoinPoint joinPointThrowing(Throwable failure) throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        when(joinPoint.proceed()).thenThrow(failure);
        return joinPoint;
    }

    private ProceedingJoinPoint joinPoint() throws NoSuchMethodException {
        Method method = LoggedOperation.class.getDeclaredMethod("execute");
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getDeclaringTypeName()).thenReturn(LoggedOperation.class.getName());
        when(signature.getName()).thenReturn(method.getName());
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        return joinPoint;
    }

    private OperateLogPayload capturedPayload() {
        ArgumentCaptor<OperateLogPayload> payloadCaptor = ArgumentCaptor.forClass(OperateLogPayload.class);
        verify(operateLogWriter).write(payloadCaptor.capture());
        return payloadCaptor.getValue();
    }

    private void assertRequestMetadata(OperateLogPayload payload, String requestMethod, String requestUri,
                                       String ip, String userAgent) {
        assertEquals(requestMethod, payload.requestMethod());
        assertEquals(requestUri, payload.requestUri());
        assertEquals(ip, payload.ip());
        assertEquals(userAgent, payload.userAgent());
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(BizLogAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(BizLogAspect.class);
        logger.detachAppender(appender);
        appender.stop();
    }

    private static class LoggedOperation {
        @BizLog(value = "测试操作", recordRequest = false, recordResponse = false)
        public String execute() {
            return "completed";
        }
    }
}
