package sm.domain.sys.base.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.common.config.CaptchaConfig;
import sm.domain.sys.base.common.constant.BaseRedisKey;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.helper.Sm2DecryptionException;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;
import sm.system.web.ClientIpResolver;
import sm.system.security.CsrfTokenManager;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

class LoginServiceTests {

	private static final String CAPTCHA_ID = "captcha-id";
	private static final String CAPTCHA_KEY = BaseRedisKey.CAPTCHA + CAPTCHA_ID;

	private final UserService userService = mock(UserService.class);
	private final LogWriteService logWriteService = mock(LogWriteService.class);
	@SuppressWarnings("unchecked")
	private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
	@SuppressWarnings("unchecked")
	private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
	private LoginService loginService;
	private final ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
	private final TemporaryLoginService temporaryLoginService = mock(TemporaryLoginService.class);
	private final CsrfTokenManager csrfTokenManager = mock(CsrfTokenManager.class);

	@BeforeEach
	void setUp() {
		loginService = new LoginService(
				mock(CaptchaConfig.class),
				userService,
				logWriteService,
				redisTemplate,
				clientIpResolver,
				temporaryLoginService,
				csrfTokenManager);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void sessionCombinesCurrentUserAndCsrfTokenWithoutNavigationData() {
		UserInfoVO user = new UserInfoVO();
		user.setId(1L);
		when(userService.current()).thenReturn(user);
		when(csrfTokenManager.getCurrentToken()).thenReturn("0123456789abcdef0123456789abcdef");

		var session = loginService.session();

		assertSame(user, session.getUser());
		assertEquals("0123456789abcdef0123456789abcdef", session.getCsrfToken());
	}

	@Test
	void temporaryCredentialUsesOneTimeGrantAuthentication() {
		LoginForm form = loginForm();
		LoginVO expected = new LoginVO();
		expected.setAuthenticated(true);
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(temporaryLoginService.supports("SMTL1.credential")).thenReturn(true);
		when(temporaryLoginService.consume("administrator", "SMTL1.credential")).thenReturn(expected);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("ABCD");
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password"))
					.thenReturn("SMTL1.credential");

			LoginVO actual = loginService.login(form);

			assertSame(expected, actual);
			verify(temporaryLoginService).consume("administrator", "SMTL1.credential");
			verify(userService, never()).authenticate("administrator", "SMTL1.credential");
		}
	}

	@Test
	void expiredCaptchaStopsAuthentication() {
		LoginForm form = loginForm();
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn(null);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("ABCD");

			BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

			assertEquals(ResultEnum.CAPTCHA_EXPIRE.getCode(), exception.getCode());
			verify(userService, never()).authenticate("administrator", "password");
			verify(redisTemplate, never()).delete(CAPTCHA_KEY);
		}
	}

	@Test
	void incorrectCaptchaStopsAuthenticationAndKeepsCaptchaForRetry() {
		LoginForm form = loginForm();
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("WXYZ");

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("ABCD");

			BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

			assertEquals(ResultEnum.CAPTCHA_ERROR.getCode(), exception.getCode());
			verify(userService, never()).authenticate("administrator", "password");
			verify(redisTemplate, never()).delete(CAPTCHA_KEY);
		}
	}

	@Test
	void validCaptchaIsConsumedAndDecryptedPasswordIsAuthenticated() {
		LoginForm form = loginForm();
		LoginVO expected = new LoginVO();
		expected.setAuthenticated(true);
		UserAuthentication authentication =
				new UserAuthentication(1L, "administrator", "管理员", false, true, 10L, null);
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(userService.authenticate("administrator", "password")).thenReturn(authentication);
		when(userService.completeLogin(authentication)).thenReturn(expected);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("abcd");
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");

			LoginVO actual = loginService.login(form);

			assertSame(expected, actual);
			verify(redisTemplate).delete(CAPTCHA_KEY);
			verify(userService).authenticate("administrator", "password");
			verify(userService).completeLogin(authentication);
		}
	}

	@Test
	void authenticationFailureIsReturnedAndWrittenToLoginLog() {
		LoginForm form = loginForm();
		UserAuthentication failed = UserAuthentication.failed("用户名或密码错误");
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(userService.authenticate("administrator", "password")).thenReturn(failed);
		when(request.getHeader("User-Agent")).thenReturn("test-agent");

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class);
			 MockedStatic<ServletUtil> servletUtil = mockStatic(ServletUtil.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("ABCD");
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");
			servletUtil.when(ServletUtil::getRequest).thenReturn(request);
			when(clientIpResolver.resolveCurrentRequest()).thenReturn("127.0.0.1");

			LoginVO actual = loginService.login(form);

			assertEquals("用户名或密码错误", actual.getMsg());
			verify(logWriteService).writeLoginFailed(
					"administrator",
					"用户名或密码错误",
					"127.0.0.1",
					"test-agent");
		}
	}

	@Test
	void passwordResetLoginReturnsOneTimeTicketWithoutCompletingLogin() {
		LoginForm form = loginForm();
		UserAuthentication authentication =
				new UserAuthentication(9L, "reset-user", "待改密用户", true, false, 10L, null);
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(userService.authenticate("administrator", "password")).thenReturn(authentication);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha")).thenReturn("ABCD");
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-password")).thenReturn("password");

			LoginVO actual = loginService.login(form);

			assertEquals(true, actual.getPasswordReset());
			verify(userService, never()).completeLogin(authentication);
			verify(valueOperations).set(
					org.mockito.ArgumentMatchers.startsWith(BaseRedisKey.PASSWORD_CHANGE_TICKET),
					eq(9L),
					eq(5L),
					eq(java.util.concurrent.TimeUnit.MINUTES));
		}
	}

	@Test
	void changePasswordConsumesTicketAndDelegatesWithDecryptedPassword() {
		PasswordChangeForm form = new PasswordChangeForm();
		form.setTicket("ticket");
		form.setNewPassword("encrypted-new-password");
		when(valueOperations.getAndDelete(BaseRedisKey.PASSWORD_CHANGE_TICKET + "ticket"))
				.thenReturn(9L);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-new-password")).thenReturn("new-password");

			loginService.changePassword(form);

			verify(userService).changeResetPassword(9L, "new-password");
		}
	}

	@Test
	void expiredPasswordChangeTicketCannotChangePassword() {
		PasswordChangeForm form = new PasswordChangeForm();
		form.setTicket("expired-ticket");
		form.setNewPassword("encrypted-new-password");
		when(valueOperations.getAndDelete(BaseRedisKey.PASSWORD_CHANGE_TICKET + "expired-ticket"))
				.thenReturn(null);

		BizException exception;
		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-new-password")).thenReturn("new-password");
			exception = assertThrows(BizException.class, () -> loginService.changePassword(form));
		}

		assertEquals(ResultEnum.UNAUTHORIZED.getCode(), exception.getCode());
		verify(userService, never()).changeResetPassword(9L, "new-password");
	}

	@Test
	void invalidSm2CiphertextIsAControlledLoginFailure() {
		LoginForm form = loginForm();

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decryptJsCiphertext("encrypted-captcha"))
					.thenThrow(new Sm2DecryptionException("SM2 密文格式无效", null));

			BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

			assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
			verify(userService, never()).authenticate("administrator", "password");
			verify(redisTemplate, never()).delete(CAPTCHA_KEY);
			verify(logWriteService).writeLoginFailed(eq("administrator"), eq("登录加密数据无效"), eq(null), eq(null));
		}
	}

	private LoginForm loginForm() {
		LoginForm form = new LoginForm();
		form.setUsername("administrator");
		form.setPassword("encrypted-password");
		form.setCaptcha("encrypted-captcha");
		form.setCaptchaId(CAPTCHA_ID);
		return form;
	}
}
