package sm.domain.sys.base.login.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.common.constant.BaseRedisKey;
import sm.domain.sys.base.login.model.form.CaptchaChallengeForm;
import sm.domain.sys.base.login.model.form.CaptchaVerifyForm;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.vo.CaptchaChallengeVO;
import sm.domain.sys.base.login.model.vo.CaptchaTicketVO;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.login.model.vo.SessionVO;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.helper.Sm2DecryptionException;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;
import sm.system.web.ClientIpResolver;
import sm.system.security.CsrfTokenManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
	private static final long PASSWORD_CHANGE_TICKET_MINUTES = 5;
	private final UserService userService;
	private final LogWriteService logWriteService;
	private final RedisTemplate<String, Object> redisTemplate;
	private final ClientIpResolver clientIpResolver;
	private final TemporaryLoginService temporaryLoginService;
	private final CsrfTokenManager csrfTokenManager;
	private final SliderCaptchaGateway sliderCaptchaGateway;
	private final LoginProtectionService loginProtectionService;
	private final LoginRedisAccessor loginRedisAccessor;

	public SessionVO session() {
		return new SessionVO(userService.current(), csrfTokenManager.getCurrentToken());
	}

	public LoginVO login(LoginForm form) {
		String clientIp = clientIpResolver.resolveCurrentRequest();
		loginProtectionService.consumeCaptchaTicket(form.getUsername(), clientIp, form.getCaptchaTicket());

		// SM2 解密前端密码
		String decryptedPassword;
		try {
			decryptedPassword = decryptLoginPayload(form.getPassword(), form.getUsername());
		} catch (BizException exception) {
			loginProtectionService.recordAuthenticationFailure(form.getUsername(), clientIp);
			throw exception;
		}
		if (temporaryLoginService.supports(decryptedPassword)) {
			try {
				LoginVO login = temporaryLoginService.consume(form.getUsername(), decryptedPassword);
				loginProtectionService.clearAfterSuccess(form.getUsername(), clientIp);
				return login;
			} catch (BizException exception) {
				loginProtectionService.recordAuthenticationFailure(form.getUsername(), clientIp);
				writeLoginFailure(form.getUsername(), "用户名或密码错误");
				throw exception;
			}
		}
		var authentication = userService.authenticate(form.getUsername(), decryptedPassword);
		if (!authentication.successful()) {
			loginProtectionService.recordAuthenticationFailure(form.getUsername(), clientIp);
			String publicMessage = credentialFailure(authentication.message())
					? "用户名或密码错误"
					: authentication.message();
			LoginVO failed = new LoginVO(publicMessage);
			if (StringUtils.hasText(form.getUsername())) {
				writeLoginFailure(form.getUsername(), authentication.message());
			}
			return failed;
		}
		if (authentication.passwordReset()) {
			loginProtectionService.clearAfterSuccess(form.getUsername(), clientIp);
			writePasswordChangeRequired(authentication);
			String ticket = UUID.randomUUID().toString();
			redisTemplate.opsForValue().set(
					BaseRedisKey.PASSWORD_CHANGE_TICKET + ticket,
					authentication.userId(),
					PASSWORD_CHANGE_TICKET_MINUTES,
					TimeUnit.MINUTES);
			LoginVO passwordReset = new LoginVO();
			passwordReset.setPasswordReset(true);
			passwordReset.setPasswordChangeTicket(ticket);
			return passwordReset;
		}
		LoginVO login = userService.completeLogin(authentication);
		loginProtectionService.clearAfterSuccess(form.getUsername(), clientIp);
		return login;
	}

	public CaptchaChallengeVO createCaptchaChallenge(CaptchaChallengeForm form) {
		String clientIp = clientIpResolver.resolveCurrentRequest();
		loginProtectionService.prepareChallenge(form.getUsername(), clientIp);
		return sliderCaptchaGateway.createChallenge();
	}

	public CaptchaTicketVO verifyCaptcha(CaptchaVerifyForm form) {
		if (!sliderCaptchaGateway.verify(form.getChallengeId(), form.getTrack())) {
			throw new BizException(ResultEnum.CAPTCHA_ERROR, "滑块位置或轨迹校验失败");
		}
		return new CaptchaTicketVO(loginProtectionService.issueCaptchaTicket(form.getUsername()));
	}

	/**
	 * 一次性凭证先原子取出再修改密码；无论后续成功与否都不能重放。
	 */
	public void changePassword(PasswordChangeForm form) {
		// 先验证并解密请求，再消费一次性凭证，避免畸形密文无意义地作废合法凭证。
		String newPassword = decryptLoginPayload(form.getNewPassword(), null);
		Object userIdValue = loginRedisAccessor.getAndDelete(
				BaseRedisKey.PASSWORD_CHANGE_TICKET + form.getTicket());
		if (userIdValue == null) {
			throw new BizException(ResultEnum.UNAUTHORIZED, "改密凭证已失效，请重新登录");
		}
		userService.changeResetPassword(Long.valueOf(String.valueOf(userIdValue)), newPassword);
	}

	private String decryptLoginPayload(String ciphertext, String username) {
		try {
			return SM2Helper.decryptJsCiphertext(ciphertext);
		} catch (Sm2DecryptionException exception) {
			log.warn("登录请求 SM2 密文无效: {}", exception.getMessage());
			if (StringUtils.hasText(username)) {
				writeLoginFailure(username, "登录加密数据无效");
			}
			throw new BizException(ResultEnum.PARAM_ERROR, "登录数据无效，请刷新页面后重试");
		}
	}

	private void writeLoginFailure(String username, String message) {
		RequestMeta requestMeta = requestMeta();
		logWriteService.writeLoginFailed(username, message, requestMeta.ip(), requestMeta.userAgent());
	}

	private void writePasswordChangeRequired(sm.domain.sys.base.user.model.vo.UserAuthentication authentication) {
		RequestMeta requestMeta = requestMeta();
		logWriteService.writeAuthenticationEvent(authentication.userId(), authentication.username(),
				authentication.name(), LoginEventType.PASSWORD_CHANGE_REQUIRED, true, null,
				requestMeta.ip(), requestMeta.userAgent());
	}

	private RequestMeta requestMeta() {
		String ip = null;
		String userAgent = null;
		try {
			ip = clientIpResolver.resolveCurrentRequest();
			userAgent = ServletUtil.getRequest().getHeader("User-Agent");
		} catch (Exception e) {
			log.warn("获取客户端IP/UA失败: {}", e.getMessage());
		}
		return new RequestMeta(ip, userAgent);
	}

	private record RequestMeta(String ip, String userAgent) { }

	public void logout() {
		StpUtil.logout();
	}

	private boolean credentialFailure(String message) {
		return !StringUtils.hasText(message) || "用户名或密码错误".equals(message);
	}
}
