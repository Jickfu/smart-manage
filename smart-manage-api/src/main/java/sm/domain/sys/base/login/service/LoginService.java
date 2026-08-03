package sm.domain.sys.base.login.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.common.config.CaptchaConfig;
import sm.domain.sys.base.common.constant.RedisKeyConstant;
import sm.domain.sys.base.common.util.CaptchaUtil;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.vo.CaptchaVO;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
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
	private final CaptchaConfig captchaConfig;
	private final UserService userService;
	private final MenuService menuService;
	private final LogWriteService logWriteService;
	private final RedisTemplate<String, Object> redisTemplate;

	public LoginVO login(LoginForm form) {
		// 验证码校验
		String decryptedCaptcha = form.getCaptcha() != null ? SM2Helper.decrypt(form.getCaptcha()) : null;
		String captchaKey = RedisKeyConstant.CAPTCHA + form.getCaptchaId();
		String captcha = (String) redisTemplate.opsForValue().get(captchaKey);
		if (captcha == null) {
			throw new BizException(ResultEnum.CAPTCHA_EXPIRE);
		}
		if (!captcha.equalsIgnoreCase(decryptedCaptcha)) {
			throw new BizException(ResultEnum.CAPTCHA_ERROR);
		}
		redisTemplate.delete(captchaKey);

		// SM2 解密前端密码
		String decryptedPassword = SM2Helper.decrypt(form.getPassword());
		var authentication = userService.authenticate(form.getUsername(), decryptedPassword);
		if (!authentication.successful()) {
			LoginVO failed = new LoginVO(authentication.message());
			if (StringUtils.hasText(form.getUsername())) {
				writeLoginFailure(form.getUsername(), authentication.message());
			}
			return failed;
		}
		if (authentication.passwordReset()) {
			String ticket = UUID.randomUUID().toString();
			redisTemplate.opsForValue().set(
					RedisKeyConstant.PASSWORD_CHANGE_TICKET + ticket,
					authentication.userId(),
					PASSWORD_CHANGE_TICKET_MINUTES,
					TimeUnit.MINUTES);
			LoginVO passwordReset = new LoginVO();
			passwordReset.setPasswordReset(true);
			passwordReset.setPasswordChangeTicket(ticket);
			return passwordReset;
		}
		return userService.completeLogin(authentication);
	}

	/**
	 * 一次性凭证先原子取出再修改密码；无论后续成功与否都不能重放。
	 */
	public void changePassword(PasswordChangeForm form) {
		Object userIdValue = redisTemplate.opsForValue().getAndDelete(
				RedisKeyConstant.PASSWORD_CHANGE_TICKET + form.getTicket());
		if (userIdValue == null) {
			throw new BizException(ResultEnum.UNAUTHORIZED, "改密凭证已失效，请重新登录");
		}
		String newPassword = SM2Helper.decrypt(form.getNewPassword());
		userService.changeResetPassword(Long.valueOf(String.valueOf(userIdValue)), newPassword);
	}

	private void writeLoginFailure(String username, String message) {
			String ip = null;
			String ua = null;
			try {
				ip = ServletUtil.getClientIp();
				ua = ServletUtil.getRequest().getHeader("User-Agent");
			} catch (Exception e) {
				log.warn("获取客户端IP/UA失败", e);
			}
			logWriteService.writeLoginFailed(username, message, ip, ua);
	}

	public CaptchaVO captcha() throws IOException {
		// 生成验证码ID
		String captchaId = UUID.randomUUID().toString();
		// 生成验证码
		String captcha = CaptchaUtil.generateCharCaptcha(captchaConfig.getLength());
		// 生成验证码图片
		BufferedImage image = CaptchaUtil.generateCaptchaImage(captcha, captchaConfig.getWidth(), captchaConfig.getHeight());

		// 将验证码存入Redis
		redisTemplate.opsForValue().set(RedisKeyConstant.CAPTCHA + captchaId, captcha, captchaConfig.getExpire(), TimeUnit.SECONDS);

		// 将图片转换为Base64
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", outputStream);
		String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
		String imageData = "data:image/jpeg;base64," + base64Image;

		// 返回VO对象
		CaptchaVO vo = new CaptchaVO();
		vo.setCaptchaId(captchaId);
		vo.setImageData(imageData);
		return vo;
	}


	public void logout() {
		StpUtil.logout();
	}
}
