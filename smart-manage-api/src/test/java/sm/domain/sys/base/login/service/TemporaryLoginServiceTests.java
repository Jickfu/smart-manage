package sm.domain.sys.base.login.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.login.model.TemporaryLoginGrant;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.system.exception.BizException;
import sm.system.web.ClientIpResolver;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemporaryLoginServiceTests {
    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    private final UserService userService = mock(UserService.class);
    private final TemporaryLoginService service = new TemporaryLoginService(
            redisTemplate,
            mock(CurrentUserContext.class),
            userService,
            mock(LogWriteService.class),
            mock(ClientIpResolver.class));

    @Test
    void recognizesOnlyVersionedTemporaryCredential() {
        assertTrue(service.supports("SMTL1.random"));
        assertEquals(false, service.supports("ordinary-password"));
    }

    @Test
    void usernameMismatchStillConsumesGrantAndCannotCreateSession() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TemporaryLoginGrant grant = new TemporaryLoginGrant("grant", 1L, 9L, "target",
                "排障", LocalDateTime.now().plusMinutes(5));
        when(valueOperations.getAndDelete(startsWith("sys:base:temporary-login:"))).thenReturn(grant);

        assertThrows(BizException.class, () -> service.consume("other-user", "SMTL1.random"));

        verify(valueOperations).getAndDelete(startsWith("sys:base:temporary-login:"));
        verify(userService, never()).authenticateTemporaryLogin(9L, "target");
    }
}
