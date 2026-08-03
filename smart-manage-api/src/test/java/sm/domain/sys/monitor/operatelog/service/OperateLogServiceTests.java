package sm.domain.sys.monitor.operatelog.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.monitor.operatelog.mapper.OperateLogMapper;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;
import sm.domain.sys.monitor.operatelog.model.form.OperateLogListForm;
import sm.system.exception.BizException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperateLogServiceTests {
    private final OperateLogMapper mapper = mock(OperateLogMapper.class);
    private final OperateLogService service = new OperateLogService(mapper, new OperateLogConverterImpl());

    @Test
    void invalidTimeRangeIsRejectedBeforeQuerying() {
        OperateLogListForm form = new OperateLogListForm();
        form.setBeginTime(LocalDateTime.of(2026, 7, 30, 12, 0));
        form.setEndTime(LocalDateTime.of(2026, 7, 30, 11, 0));

        assertThrows(BizException.class, () -> service.listPage(form));
    }

    @Test
    void detailContainsTraceAndSanitizedPayloadContract() {
        OperateLogEntity entity = new OperateLogEntity();
        entity.setId(20L);
        entity.setBizName("保存用户");
        entity.setRequestParams("{\"password\":\"***\"}");
        entity.setTraceId("trace-operate");
        when(mapper.selectById(20L)).thenReturn(entity);

        var detail = service.detail(20L);

        assertEquals("保存用户", detail.getBizName());
        assertEquals("{\"password\":\"***\"}", detail.getRequestParams());
        assertEquals("trace-operate", detail.getTraceId());
    }

    @Test
    void missingDetailIsReportedExplicitly() {
        when(mapper.selectById(20L)).thenReturn(null);

        assertThrows(BizException.class, () -> service.detail(20L));
    }
}
