package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.message.email.contract.EmailNotificationSender;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.crypto.BrowserPasswordCipher;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserEmailPasswordServiceTests {

    @Test
    void administratorCannotUseCurrentEmailPasswordFlow() {
        Fixture fixture = new Fixture();
        UserEntity administrator = eligibleUser("administrator");
        when(fixture.currentUserContext.getUserId()).thenReturn(1L);
        when(fixture.userMapper.selectById(1L)).thenReturn(administrator);

        assertThrows(BizException.class, fixture.service::requestCurrentCode);
        verify(fixture.emailSender, never()).enqueueSensitive(any());
    }

    @Test
    void unknownPublicEmailDoesNotCreateEmailTask() {
        Fixture fixture = new Fixture();
        when(fixture.sysParamService.getInt(LoginProtectionParam.PASSWORD_EMAIL_CODE_RESEND_SECONDS))
                .thenReturn(60);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(fixture.redisTemplate.opsForValue()).thenReturn(valueOperations);

        fixture.service.requestPublicCode("missing@example.com");

        verify(fixture.emailSender, never()).enqueueSensitive(any());
    }

    @Test
    void successfulCodeConsumptionChangesPasswordAndTerminatesSessions() {
        Fixture fixture = new Fixture();
        UserEntity user = eligibleUser("user");
        when(fixture.userMapper.selectOne(any())).thenReturn(user);
        when(fixture.sysParamService.getInt(LoginProtectionParam.PASSWORD_EMAIL_CODE_MAX_ATTEMPTS))
                .thenReturn(5);
        when(fixture.sysParamService.getInt(LoginProtectionParam.PASSWORD_EMAIL_CODE_EXPIRE_MINUTES))
                .thenReturn(10);
        when(fixture.redisTemplate.execute(any(), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(fixture.browserPasswordCipher.decrypt("encrypted-new-password")).thenReturn("new-password");

        fixture.service.resetPublicPassword(
                "USER@EXAMPLE.COM", "123456", "encrypted-new-password");

        verify(fixture.txService).updatePasswordByVerifiedEmail(1L, "new-password");
        verify(fixture.authorizationStateHelper).terminateUsers(anyList(), any());
    }

    private static UserEntity eligibleUser(String username) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("user@example.com");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEnabled(true);
        return user;
    }

    private static final class Fixture {
        private final UserMapper userMapper = mock(UserMapper.class);
        private final UserTxService txService = mock(UserTxService.class);
        private final EmailNotificationSender emailSender = mock(EmailNotificationSender.class);
        private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        private final SysParamService sysParamService = mock(SysParamService.class);
        private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        private final BrowserPasswordCipher browserPasswordCipher = mock(BrowserPasswordCipher.class);
        private final AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
        private final UserAuthenticationService authenticationService = mock(UserAuthenticationService.class);
        private final UserEmailPasswordService service = new UserEmailPasswordService(
                userMapper, txService, emailSender, redisTemplate, sysParamService, currentUserContext,
                browserPasswordCipher, authorizationStateHelper, authenticationService);
    }
}
