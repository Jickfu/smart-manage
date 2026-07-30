package sm.domain.sys.monitor.loginlog.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.loginlog.model.form.LoginLogListForm;
import sm.system.exception.BizException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginLogServiceTests {
    private final LoginLogMapper mapper = mock(LoginLogMapper.class);
    private final LoginLogService service = new LoginLogService(mapper, new LoginLogConverterImpl());

    @Test
    void invalidTimeRangeIsRejectedBeforeQuerying() {
        LoginLogListForm form = new LoginLogListForm();
        form.setBeginTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        form.setEndTime(LocalDateTime.of(2026, 7, 30, 11, 0));

        assertThrows(BizException.class, () -> service.listPage(form));
    }

    @Test
    void detailContainsDiagnosticFieldsWithoutTokenData() {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setId(10L);
        entity.setUsername("administrator");
        entity.setUserAgent("test-agent");
        entity.setTraceId("trace-login");
        when(mapper.selectById(10L)).thenReturn(entity);

        var detail = service.detail(10L);

        assertEquals("administrator", detail.getUsername());
        assertEquals("test-agent", detail.getUserAgent());
        assertEquals("trace-login", detail.getTraceId());
    }

    @Test
    void missingDetailIsReportedExplicitly() {
        when(mapper.selectById(10L)).thenReturn(null);

        assertThrows(BizException.class, () -> service.detail(10L));
    }
}
