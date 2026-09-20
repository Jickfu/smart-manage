package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.message.email.contract.EmailNotificationSender;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.crypto.BrowserPasswordCipher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import sm.domain.sys.message.email.contract.SensitiveEmailNotificationCommand;

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
    void oldCodeCannotBeUsedAfterGenerationChangesEvenWithSameMailbox() {
        Fixture fixture = new Fixture();
        UserEntity user = eligibleUser("user");
        String code = fixture.issueCode(user);
        user.setCredentialGeneration(2L);
        assertThrows(BizException.class, () -> fixture.service.resetPublicPassword(
                user.getEmail(), code, "encrypted-new-password"));
        org.mockito.Mockito.verifyNoInteractions(fixture.txService);
    }

    @Test
    void recoveryProofCannotBeConsumedForCurrentPasswordOrBinding() {
        Fixture fixture = new Fixture();
        UserEntity user = eligibleUser("user");
        String code = fixture.issueCode(user);
        when(fixture.currentUserContext.getUserId()).thenReturn(1L);
        when(fixture.userMapper.selectById(1L)).thenReturn(user);
        assertThrows(BizException.class, () -> fixture.service.changeCurrentPassword(code, "encrypted-new-password"));
        assertThrows(BizException.class, () -> fixture.service.bindCurrentEmail(user.getEmail(), code));
        org.mockito.Mockito.verifyNoInteractions(fixture.txService);
    }

    @Test
    void oldMailboxCodeCannotResetPasswordAfterMailboxChanges() {
        Fixture fixture = new Fixture();
        UserEntity user = eligibleUser("user");
        String oldCode = fixture.issueCode(user);
        user.setEmail("new@example.com");

        assertThrows(BizException.class, () -> fixture.service.resetPublicPassword(
                user.getEmail(), oldCode, "encrypted-new-password"));
        org.mockito.Mockito.verifyNoInteractions(fixture.txService);
    }

    @Test
    void unchangedMailboxCodeCanOnlyBeConsumedOnce() {
        Fixture fixture = new Fixture();
        UserEntity user = eligibleUser("user");
        String code = fixture.issueCode(user);
        fixture.service.resetPublicPassword(user.getEmail(), code, "encrypted-new-password");
        assertThrows(BizException.class, () -> fixture.service.resetPublicPassword(
                user.getEmail(), code, "encrypted-new-password"));
    }

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
    void successfulCodeConsumptionChangesPasswordAndRefreshesDisplayCache() {
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

        verify(fixture.txService).updatePasswordByVerifiedEmail(new sm.domain.sys.base.user.model.UserCredentialSnapshot(1L, "user@example.com", 0L), "new-password");
        verify(fixture.userCacheInvalidator).tryRefreshUsers(anyList());
    }

    private static UserEntity eligibleUser(String username) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("user@example.com");
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setCredentialGeneration(0L);
        return user;
    }

    private static final class Fixture {
        @SuppressWarnings("unchecked")
        String issueCode(UserEntity user) {
            when(userMapper.selectOne(any())).thenReturn(user);
            when(sysParamService.getInt(anyString())).thenReturn(5);
            when(browserPasswordCipher.decrypt("encrypted-new-password")).thenReturn("new-password");
            ValueOperations<String, String> values = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(values);
            when(values.setIfAbsent(anyString(), anyString(), org.mockito.ArgumentMatchers.anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            var stored = new HashMap<String, String>();
            org.mockito.Mockito.doAnswer(call -> {
                stored.put(call.getArgument(0), call.getArgument(1));
                return null;
            }).when(values).set(anyString(), anyString(), org.mockito.ArgumentMatchers.anyLong(), any(TimeUnit.class));
            // 模拟 Lua 的真实摘要比较和成功后删除，不把消费结果硬编码为成功。
            when(redisTemplate.execute(any(), anyList(), anyString(), anyString(), anyString())).thenAnswer(call -> {
                List<String> keys = call.getArgument(1);
                String expected = stored.get(keys.get(0));
                if (expected == null || !Objects.equals(expected, call.getArgument(2))) return 0L;
                stored.remove(keys.get(0));
                return 1L;
            });
            var code = new AtomicReference<String>();
            when(emailSender.enqueueSensitive(any())).thenAnswer(call -> {
                SensitiveEmailNotificationCommand command = call.getArgument(0);
                var matcher = java.util.regex.Pattern.compile("\\d{6}").matcher(command.textBody());
                org.junit.jupiter.api.Assertions.assertTrue(matcher.find());
                code.set(matcher.group());
                return 1L;
            });
            service.requestPublicCode(user.getEmail());
            org.junit.jupiter.api.Assertions.assertNotNull(code.get());
            return code.get();
        }

        private final UserMapper userMapper = mock(UserMapper.class);
        private final UserTxService txService = mock(UserTxService.class);
        private final EmailNotificationSender emailSender = mock(EmailNotificationSender.class);
        private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        private final SysParamService sysParamService = mock(SysParamService.class);
        private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        private final BrowserPasswordCipher browserPasswordCipher = mock(BrowserPasswordCipher.class);
        private final UserCacheInvalidator userCacheInvalidator = mock(UserCacheInvalidator.class);
        private final UserAuthenticationService authenticationService = mock(UserAuthenticationService.class);
        private final UserEmailPasswordService service = new UserEmailPasswordService(
                userMapper, txService, emailSender, redisTemplate, sysParamService, currentUserContext,
                browserPasswordCipher, userCacheInvalidator, authenticationService);
    }
}
