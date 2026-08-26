package sm.domain.sys.base.login.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.login.model.TemporaryLoginGrant;
import sm.domain.sys.base.user.service.UserAuthenticationService;
import sm.domain.sys.base.user.service.UserCacheAccessor;
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
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final UserAuthenticationService userAuthenticationService = mock(UserAuthenticationService.class);
    private final UserCacheAccessor userCacheAccessor = mock(UserCacheAccessor.class);
    private final UserSessionService userSessionService = mock(UserSessionService.class);
    private final LoginRedisAccessor loginRedisAccessor = mock(LoginRedisAccessor.class);
    private final LoginCacheJsonCodec cacheJsonCodec = mock(LoginCacheJsonCodec.class);
    private final TemporaryLoginService service = new TemporaryLoginService(
            redisTemplate,
            mock(CurrentUserContext.class),
            userAuthenticationService,
            userCacheAccessor,
            userSessionService,
            mock(LogWriteService.class),
            mock(ClientIpResolver.class),
            loginRedisAccessor,
            cacheJsonCodec);

    @Test
    void recognizesOnlyVersionedTemporaryCredential() {
        assertTrue(service.supports("SMTL1.random"));
        assertEquals(false, service.supports("ordinary-password"));
    }

    @Test
    void usernameMismatchStillConsumesGrantAndCannotCreateSession() {
        TemporaryLoginGrant grant = new TemporaryLoginGrant("grant", 1L, 9L, "target",
                "排障", LocalDateTime.now().plusMinutes(5));
        when(loginRedisAccessor.getAndDelete(startsWith("sys:base:temporary-login:"))).thenReturn("grant-json");
        when(cacheJsonCodec.read("grant-json", TemporaryLoginGrant.class)).thenReturn(grant);

        assertThrows(BizException.class, () -> service.consume("other-user", "SMTL1.random"));

        verify(loginRedisAccessor).getAndDelete(startsWith("sys:base:temporary-login:"));
        verify(userAuthenticationService, never()).authenticateTemporaryLogin(9L, "target");
    }
}
