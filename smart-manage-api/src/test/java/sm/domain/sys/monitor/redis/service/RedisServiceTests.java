package sm.domain.sys.monitor.redis.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisServiceTests {

    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final RedisService service = new RedisService(redisTemplate, currentUserContext);

    @Test
    void sensitiveAuthenticationValueMustNeverBeReturned() {
        BizException exception = assertThrows(BizException.class,
                () -> service.value("satoken:login:token-session:secret"));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), exception.getCode());
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void deleteMustCheckAdministratorAndKeepBatchBoundedByValidatedForm() {
        when(redisTemplate.delete(List.of("example"))).thenReturn(1L);

        assertEquals(1L, service.delete(List.of("example")));
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void deleteMustRejectOversizedBatchAtServiceBoundary() {
        List<String> keys = IntStream.rangeClosed(1, 101).mapToObj(index -> "key-" + index).toList();

        assertThrows(BizException.class, () -> service.delete(keys));
        verify(currentUserContext).checkAdministrator();
    }
}
