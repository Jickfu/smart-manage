package sm.domain.sys.base.sysparam.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SysParamCacheAccessorTests {

    @Test
    void loadsNumberValueSnapshotFromMapper() {
        SysParamMapper mapper = mock(SysParamMapper.class);
        SysParamEntity timeout = new SysParamEntity();
        timeout.setNumber("SCRIPT_CONSOLE_TIMEOUT_SECONDS");
        timeout.setValue("30");
        when(mapper.selectList(null)).thenReturn(List.of(timeout));

        Map<String, String> parameters = new SysParamCacheAccessor(mapper).getAll();

        assertEquals(Map.of("SCRIPT_CONSOLE_TIMEOUT_SECONDS", "30"), parameters);
    }
}
