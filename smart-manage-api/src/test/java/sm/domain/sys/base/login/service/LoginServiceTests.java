package sm.domain.sys.base.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.base.common.config.CaptchaConfig;
import sm.domain.sys.base.common.constant.RedisKeyConstant;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTests {

	private static final String CAPTCHA_ID = "captcha-id";
	private static final String CAPTCHA_KEY = RedisKeyConstant.CAPTCHA + CAPTCHA_ID;

	private final UserService userService = mock(UserService.class);
	private final LogWriteService logWriteService = mock(LogWriteService.class);
	@SuppressWarnings("unchecked")
	private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
	@SuppressWarnings("unchecked")
	private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
	private LoginService loginService;

	@BeforeEach
	void setUp() {
		loginService = new LoginService(
				mock(CaptchaConfig.class),
				userService,
				mock(MenuService.class),
				logWriteService);
		ReflectionTestUtils.setField(loginService, "redisTemplate", redisTemplate);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void expiredCaptchaStopsAuthentication() {
		LoginForm form = loginForm();
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn(null);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-captcha")).thenReturn("ABCD");

			BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

			assertEquals(ResultEnum.CAPTCHA_EXPIRE.getCode(), exception.getCode());
			verify(userService, never()).login("administrator", "password");
			verify(redisTemplate, never()).delete(CAPTCHA_KEY);
		}
	}

	@Test
	void incorrectCaptchaStopsAuthenticationAndKeepsCaptchaForRetry() {
		LoginForm form = loginForm();
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("WXYZ");

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-captcha")).thenReturn("ABCD");

			BizException exception = assertThrows(BizException.class, () -> loginService.login(form));

			assertEquals(ResultEnum.CAPTCHA_ERROR.getCode(), exception.getCode());
			verify(userService, never()).login("administrator", "password");
			verify(redisTemplate, never()).delete(CAPTCHA_KEY);
		}
	}

	@Test
	void validCaptchaIsConsumedAndDecryptedPasswordIsAuthenticated() {
		LoginForm form = loginForm();
		LoginVO expected = new LoginVO();
		expected.setToken("token");
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(userService.login("administrator", "password")).thenReturn(expected);

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class)) {
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-captcha")).thenReturn("abcd");
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-password")).thenReturn("password");

			LoginVO actual = loginService.login(form);

			assertSame(expected, actual);
			verify(redisTemplate).delete(CAPTCHA_KEY);
			verify(userService).login("administrator", "password");
		}
	}

	@Test
	void authenticationFailureIsReturnedAndWrittenToLoginLog() {
		LoginForm form = loginForm();
		LoginVO expected = new LoginVO("用户名或密码错误");
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(valueOperations.get(CAPTCHA_KEY)).thenReturn("ABCD");
		when(userService.login("administrator", "password")).thenReturn(expected);
		when(request.getHeader("User-Agent")).thenReturn("test-agent");

		try (MockedStatic<SM2Helper> sm2Helper = mockStatic(SM2Helper.class);
			 MockedStatic<ServletUtil> servletUtil = mockStatic(ServletUtil.class)) {
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-captcha")).thenReturn("ABCD");
			sm2Helper.when(() -> SM2Helper.decrypt("encrypted-password")).thenReturn("password");
			servletUtil.when(ServletUtil::getClientIp).thenReturn("127.0.0.1");
			servletUtil.when(ServletUtil::getRequest).thenReturn(request);

			LoginVO actual = loginService.login(form);

			assertSame(expected, actual);
			verify(logWriteService).writeLoginFailed(
					"administrator",
					"用户名或密码错误",
					"127.0.0.1",
					"test-agent");
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
