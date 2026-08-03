package sm.domain.sys.monitor.cache.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.domain.sys.monitor.redis.service.RedisService;
import tools.jackson.databind.json.JsonMapper;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.system.response.ResultEnum;

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
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final RedisService redisService = mock(RedisService.class);
    private final CacheService service = new CacheService(
            cacheHelper, redisTemplate, currentUserContext, redisService, JsonMapper.builder().build());

    @Test
    void clearMustRejectCachesOutsideManagedCatalog() {
        assertThrows(BizException.class, () -> service.clear("unmanaged-cache"));
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void sensitiveLocalCacheValueMustNeverBeReturned() {
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("LOCAL");
        form.setCacheName(CacheConstant.FILE_CONFIG);
        form.setKey("default");

        BizException exception = assertThrows(BizException.class, () -> service.value(form));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), exception.getCode());
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void unsupportedMemoryCommandMustDegradeWithoutRepeatedExecution() {
        RedisConnection connection = mock(RedisConnection.class);
        byte[] keyBytes = "example".getBytes();
        when(connection.execute(eq("MEMORY"), any(byte[].class), same(keyBytes)))
                .thenThrow(new RedisSystemException("Error in execution",
                        new RuntimeException("ERR unknown command 'MEMORY'")));

        assertNull(service.readMemoryUsage(connection, keyBytes));
        assertNull(service.readMemoryUsage(connection, keyBytes));

        verify(connection, times(1)).execute(eq("MEMORY"), any(byte[].class), same(keyBytes));
    }
}
