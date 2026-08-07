package sm.domain.sys.base.sysparam.service;

import com.alicp.jetcache.anno.Cached;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysParamServiceTests {

    @Test
    void cacheAccessorMustRemainAnAnnotatedSpringProxyBoundary() throws NoSuchMethodException {
        assertNotNull(SysParamCacheAccessor.class.getAnnotation(Component.class));
        assertNotNull(SysParamCacheAccessor.class.getMethod("getAll").getAnnotation(Cached.class));
    }

    @Test
    void readsIntegerThroughCacheAccessor() {
        SysParamCacheAccessor cacheAccessor = mock(SysParamCacheAccessor.class);
        when(cacheAccessor.getAll()).thenReturn(Map.of("SCRIPT_CONSOLE_TIMEOUT_SECONDS", "45"));
        SysParamService service = new SysParamService(
                mock(SysParamMapper.class), mock(SysParamTxService.class), cacheAccessor);

        Integer value = service.getInt("SCRIPT_CONSOLE_TIMEOUT_SECONDS");

        assertEquals(45, value);
        verify(cacheAccessor).getAll();
    }
}
