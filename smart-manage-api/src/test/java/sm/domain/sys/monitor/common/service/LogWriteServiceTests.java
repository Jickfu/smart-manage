package sm.domain.sys.monitor.common.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.operatelog.mapper.OperateLogMapper;
import sm.system.util.TraceIdUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogWriteServiceTests {
    private final LoginLogMapper loginLogMapper = mock(LoginLogMapper.class);
    private final LogWriteService service =
            new LogWriteService(loginLogMapper, mock(OperateLogMapper.class));

    @AfterEach
    void clearTraceId() {
        TraceIdUtil.clear();
    }

    @Test
    void loginFailureCapturesTraceAndTruncatesRequestMetadata() {
        TraceIdUtil.setTraceId("trace-failure");
        String oversizedUserAgent = "a".repeat(1100);

        service.writeLoginFailed("administrator", "用户名或密码错误", "127.0.0.1", oversizedUserAgent);

        ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
        verify(loginLogMapper).insert(captor.capture());
        LoginLogEntity entity = captor.getValue();
        assertEquals(LoginEventType.LOGIN_FAILURE.name(), entity.getEventType());
        assertEquals(false, entity.getSuccess());
        assertEquals("trace-failure", entity.getTraceId());
        assertEquals(1024, entity.getUserAgent().length());
    }
}
