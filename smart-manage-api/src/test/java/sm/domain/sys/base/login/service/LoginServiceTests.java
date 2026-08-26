package sm.domain.sys.base.login.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.common.constant.BaseRedisKey;
import sm.domain.sys.base.login.model.form.CaptchaChallengeForm;
import sm.domain.sys.base.login.model.form.CaptchaTrackForm;
import sm.domain.sys.base.login.model.form.CaptchaVerifyForm;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.vo.CaptchaChallengeVO;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.service.UserAuthenticationService;
import sm.domain.sys.base.user.service.UserProfileService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.helper.Sm2DecryptionException;
import sm.system.response.ResultEnum;
import sm.system.security.CsrfTokenManager;
import sm.system.util.ServletUtil;
import sm.system.web.ClientIpResolver;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTests {
    private final UserAuthenticationService userAuthenticationService = mock(UserAuthenticationService.class);
    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final UserSessionService userSessionService = mock(UserSessionService.class);
    private final LogWriteService logWriteService = mock(LogWriteService.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
    private final TemporaryLoginService temporaryLoginService = mock(TemporaryLoginService.class);
    private final CsrfTokenManager csrfTokenManager = mock(CsrfTokenManager.class);
    private final SliderCaptchaGateway sliderCaptchaGateway = mock(SliderCaptchaGateway.class);
    private final LoginProtectionService loginProtectionService = mock(LoginProtectionService.class);
    private final LoginRedisAccessor loginRedisAccessor = mock(LoginRedisAccessor.class);
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(userAuthenticationService, userProfileService, userSessionService,
                logWriteService, redisTemplate, clientIpResolver,
                temporaryLoginService, csrfTokenManager, sliderCaptchaGateway, loginProtectionService,
                loginRedisAccessor);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(clientIpResolver.resolveCurrentRequest()).thenReturn("127.0.0.1");
    }

    @Test
    void sessionCombinesCurrentUserAndCsrfTokenWithoutNavigationData() {
        UserInfoVO user = new UserInfoVO();
        user.setId(1L);
        when(userProfileService.current()).thenReturn(user);
        when(csrfTokenManager.getCurrentToken()).thenReturn("0123456789abcdef0123456789abcdef");

        var session = loginService.session();

        assertSame(user, session.getUser());
        assertEquals("0123456789abcdef0123456789abcdef", session.getCsrfToken());
    }

    @Test
    void challengeIsRateCheckedBeforeGeneration() {
        CaptchaChallengeForm form = new CaptchaChallengeForm();
        form.setUsername("administrator");
        CaptchaChallengeVO expected = new CaptchaChallengeVO();
        when(sliderCaptchaGateway.createChallenge()).thenReturn(expected);

        assertSame(expected, loginService.createCaptchaChallenge(form));

        verify(loginProtectionService).prepareChallenge("administrator", "127.0.0.1");
    }

    @Test
    void validChallengeIssuesOneTimeTicket() {
        CaptchaVerifyForm form = captchaVerifyForm();
        when(sliderCaptchaGateway.verify("challenge-id", form.getTrack())).thenReturn(true);
        when(loginProtectionService.issueCaptchaTicket("administrator")).thenReturn("ticket");

        var result = loginService.verifyCaptcha(form);

        assertEquals("ticket", result.getCaptchaTicket());
    }

    @Test
    void invalidChallengeDoesNotIssueTicket() {
        CaptchaVerifyForm form = captchaVerifyForm();
        when(sliderCaptchaGateway.verify("challenge-id", form.getTrack())).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () -> loginService.verifyCaptcha(form));

        assertEquals(ResultEnum.CAPTCHA_ERROR.getCode(), exception.getCode());
        verify(loginProtectionService, never()).issueCaptchaTicket("administrator");
    }

    @Test
    void validTicketIsConsumedBeforePasswordAuthentication() {
        LoginForm form = loginForm();
        LoginVO expected = new LoginVO();
        expected.setAuthenticated(true);
        UserAuthentication authentication =
                new UserAuthentication(1L, "administrator", "管理员", false, true, 10L, null);
        when(userAuthenticationService.authenticate("administrator", "password")).thenReturn(authentication);
        when(userSessionService.completeLogin(authentication)).thenReturn(expected);

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");

            assertSame(expected, loginService.login(form));

            verify(loginProtectionService).consumeCaptchaTicket("administrator", "127.0.0.1", "captcha-ticket");
            verify(loginProtectionService).clearAfterSuccess("administrator", "127.0.0.1");
        }
    }

    @Test
    void temporaryCredentialUsesSameCaptchaAndProtectionFlow() {
        LoginForm form = loginForm();
        LoginVO expected = new LoginVO();
        expected.setAuthenticated(true);
        when(temporaryLoginService.supports("SMTL1.credential")).thenReturn(true);
        when(temporaryLoginService.consume("administrator", "SMTL1.credential")).thenReturn(expected);

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password"))
                    .thenReturn("SMTL1.credential");

            assertSame(expected, loginService.login(form));
            verify(loginProtectionService).clearAfterSuccess("administrator", "127.0.0.1");
            verify(userAuthenticationService, never()).authenticate("administrator", "SMTL1.credential");
        }
    }

    @Test
    void credentialFailureIsCountedAndAuditedWithUnifiedMessage() {
        LoginForm form = loginForm();
        UserAuthentication failed = UserAuthentication.failed("用户名或密码错误");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(userAuthenticationService.authenticate("administrator", "password")).thenReturn(failed);
        when(request.getHeader("User-Agent")).thenReturn("test-agent");

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class);
             MockedStatic<ServletUtil> servletUtil = mockStatic(ServletUtil.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");
            servletUtil.when(ServletUtil::getRequest).thenReturn(request);

            LoginVO actual = loginService.login(form);

            assertEquals("用户名或密码错误", actual.getMsg());
            verify(loginProtectionService).recordAuthenticationFailure("administrator", "127.0.0.1");
            verify(logWriteService).writeLoginFailed(
                    "administrator", "用户名或密码错误", "127.0.0.1", "test-agent");
        }
    }

    @Test
    void accountStateFailureKeepsActionableMessageAfterPasswordWasValid() {
        LoginForm form = loginForm();
        when(userAuthenticationService.authenticate("administrator", "password"))
                .thenReturn(UserAuthentication.failed("用户已被禁用"));

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");

            LoginVO actual = loginService.login(form);

            assertEquals("用户已被禁用", actual.getMsg());
        }
    }

    @Test
    void passwordResetLoginReturnsOneTimeTicketWithoutCompletingLogin() {
        LoginForm form = loginForm();
        UserAuthentication authentication =
                new UserAuthentication(9L, "reset-user", "待改密用户", true, false, 10L, null);
        when(userAuthenticationService.authenticate("administrator", "password")).thenReturn(authentication);

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");

            LoginVO actual = loginService.login(form);

            assertEquals(true, actual.getPasswordReset());
            verify(userSessionService, never()).completeLogin(authentication);
            verify(valueOperations).set(startsWith(BaseRedisKey.PASSWORD_CHANGE_TICKET), eq("9"), eq(5L),
                    eq(TimeUnit.MINUTES));
        }
    }

    @Test
    void changePasswordConsumesTicketAndDelegatesWithDecryptedPassword() {
        PasswordChangeForm form = new PasswordChangeForm();
        form.setTicket("ticket");
        form.setNewPassword("encrypted-new-password");
        when(loginRedisAccessor.getAndDelete(BaseRedisKey.PASSWORD_CHANGE_TICKET + "ticket")).thenReturn("9");

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-new-password"))
                    .thenReturn("new-password");

            loginService.changePassword(form);

            verify(userAuthenticationService).changeResetPassword(9L, "new-password");
        }
    }

    @Test
    void invalidSm2CiphertextIsAControlledLoginFailure() {
        LoginForm form = loginForm();

        try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
            sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password"))
                    .thenThrow(new Sm2DecryptionException("SM2 密文格式无效", null));

            BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

            assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
            verify(userAuthenticationService, never()).authenticate("administrator", "password");
        }
    }

    private LoginForm loginForm() {
        LoginForm form = new LoginForm();
        form.setUsername("administrator");
        form.setPassword("encrypted-password");
        form.setCaptchaTicket("captcha-ticket");
        return form;
    }

    private CaptchaVerifyForm captchaVerifyForm() {
        CaptchaVerifyForm form = new CaptchaVerifyForm();
        form.setUsername("administrator");
        form.setChallengeId("challenge-id");
        form.setTrack(new CaptchaTrackForm());
        return form;
    }
}
