package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import tools.jackson.databind.json.JsonMapper;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.domain.sys.monitor.cache.model.vo.CacheRuntimeVO;
import sm.system.response.ResultEnum;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CacheServiceTests {

    private final CacheHelper cacheHelper = mock(CacheHelper.class);
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final RedisCacheAccessor redisCacheAccessor = mock(RedisCacheAccessor.class);
    private final CacheService service = new CacheService(
            cacheHelper, currentUserContext, redisCacheAccessor, JsonMapper.builder().build());

    @Test
    void clearMustRejectCachesOutsideManagedCatalog() {
        assertThrows(BizException.class, () -> service.clear("unmanaged-cache"));
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void credentialBearingFileConfigMustNotBeInManagedCacheCatalog() {
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("LOCAL");
        form.setCacheName(CacheConstant.FILE_CONFIG);
        form.setKey("default");

        BizException exception = assertThrows(BizException.class, () -> service.value(form));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void runtimeMustCheckAdministratorBeforeDelegating() {
        CacheRuntimeVO runtime = CacheRuntimeVO.builder().available(true).build();
        when(redisCacheAccessor.runtime()).thenReturn(runtime);

        assertEquals(runtime, service.runtime());

        verify(currentUserContext).checkAdministrator();
        verify(redisCacheAccessor).runtime();
    }

    @Test
    void unsupportedMemoryCommandMustDegradeWithoutRepeatedExecution() {
        RedisConnection connection = mock(RedisConnection.class);
        byte[] keyBytes = "example".getBytes();
        when(connection.execute(eq("MEMORY"), any(byte[].class), same(keyBytes)))
                .thenThrow(new RedisSystemException("Error in execution",
                        new RuntimeException("ERR unknown command 'MEMORY'")));

        RedisCacheAccessor accessor = new RedisCacheAccessor(mock(org.springframework.data.redis.core.RedisTemplate.class));
        assertNull(accessor.readMemoryUsage(connection, keyBytes));
        assertNull(accessor.readMemoryUsage(connection, keyBytes));

        verify(connection, times(1)).execute(eq("MEMORY"), any(byte[].class), same(keyBytes));
    }

    @Test
    @SuppressWarnings("unchecked")
    void managedSystemParameterValueMustUseJetCacheDecoding() {
        Cache<Object, Object> cache = mock(Cache.class);
        when(cacheHelper.getCache(CacheConstant.SYS_PARAM, com.alicp.jetcache.anno.CacheType.REMOTE))
                .thenReturn(cache);
        when(cache.get("all")).thenReturn(Map.of("SCRIPT_CONSOLE_TIMEOUT_SECONDS", "30"));
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("REDIS");
        form.setCacheName(CacheConstant.SYS_PARAM);
        form.setKey(CacheConstant.SYS_PARAM + "all");

        var result = service.value(form);

        assertEquals("object", result.getType());
        assertEquals(false, result.getItems().getFirst().isBase64());
        assertEquals("{\"SCRIPT_CONSOLE_TIMEOUT_SECONDS\":\"30\"}", result.getItems().getFirst().getValue());
        verify(cache).get("all");
        verify(redisCacheAccessor, org.mockito.Mockito.never()).value(any());
    }

    @Test
    void managedSensitiveRemoteCacheMustNotBeDecoded() {
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("REDIS");
        form.setCacheName(CacheConstant.USER_INFO);
        form.setKey(CacheConstant.USER_INFO + "1");

        BizException exception = assertThrows(BizException.class, () -> service.value(form));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), exception.getCode());
        verify(redisCacheAccessor, org.mockito.Mockito.never()).value(any());
    }
}
