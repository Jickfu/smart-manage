package sm.domain.sys.monitor.cache.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheAccessorTests {

    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final RedisCacheAccessor accessor = new RedisCacheAccessor(redisTemplate);

    @Test
    void sensitiveAuthenticationValueMustNeverBeReturned() {
        BizException exception = assertThrows(BizException.class,
                () -> accessor.value("satoken:login:token-session:secret"));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), exception.getCode());
    }

    @Test
    void applicationTokenNamespaceMustNeverBeReturned() {
        BizException exception = assertThrows(BizException.class,
                () -> accessor.value("smtoken:login:last-active:secret"));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), exception.getCode());
    }

    @Test
    void deleteMustKeepBatchBounded() {
        when(redisTemplate.delete(List.of("example"))).thenReturn(1L);

        assertEquals(1L, accessor.delete(List.of("example")));
    }

    @Test
    void deleteMustRejectOversizedBatchAtServiceBoundary() {
        List<String> keys = IntStream.rangeClosed(1, 101).mapToObj(index -> "key-" + index).toList();

        assertThrows(BizException.class, () -> accessor.delete(keys));
    }

    @Test
    void commandResultMustAcceptJedisObjectArrayShape() {
        Object[] rawResult = {"0".getBytes(), new Object[]{"user-info1".getBytes()}};

        List<?> result = RedisCacheAccessor.asList(rawResult, "unexpected");

        assertEquals(2, result.size());
        assertEquals(1, RedisCacheAccessor.asList(result.get(1), "unexpected").size());
    }
}
