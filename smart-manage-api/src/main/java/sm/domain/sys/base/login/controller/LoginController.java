package sm.domain.sys.base.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.login.model.form.CaptchaChallengeForm;
import sm.domain.sys.base.login.model.form.CaptchaVerifyForm;
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.form.EmailPasswordCodeRequestForm;
import sm.domain.sys.base.login.model.form.EmailPasswordResetForm;
import sm.domain.sys.base.login.model.vo.CaptchaChallengeVO;
import sm.domain.sys.base.login.model.vo.CaptchaTicketVO;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.login.model.vo.SessionVO;
import sm.domain.sys.base.login.service.LoginService;
import sm.system.response.Result;

/**
 * @author Chekfu
 */
@RestController
@Tag(name = "认证管理", description = "用户登录/注册/登出相关接口")
@RequiredArgsConstructor
public class LoginController {
	private final LoginService service;

	@Operation(summary = "当前会话", description = "获取当前登录用户和会话绑定的 CSRF Token")
	@GetMapping("/sys/base/session")
	public Result<SessionVO> session() {
		return Result.success(service.session());
	}

	@Operation(summary = "用户登录", description = "通过用户名密码建立浏览器登录会话")
	@PostMapping("/sys/base/login")
	@SaIgnore
	public Result<LoginVO> login(@Parameter(description = "登录表单", required = true) @Validated @RequestBody LoginForm form) {
		return Result.success(service.login(form));
	}

	@SaIgnore
	@Operation(summary = "首次登录修改密码", description = "使用一次性改密凭证设置正式密码")
	@PostMapping("/sys/base/login/change-password")
	public Result<String> changePassword(@Validated @RequestBody PasswordChangeForm form) {
		service.changePassword(form);
		return Result.success();
	}

	@SaIgnore
	@Operation(summary = "发送邮箱找回验证码", description = "消费滑块票据后统一响应，避免暴露邮箱对应账号状态")
	@PostMapping("/sys/base/login/password/email/code")
	public Result<String> requestEmailPasswordCode(
			@Validated @RequestBody EmailPasswordCodeRequestForm form) {
		service.requestEmailPasswordCode(form);
		return Result.success(sm.domain.sys.base.user.service.UserEmailPasswordService.GENERIC_SEND_MESSAGE);
	}

	@SaIgnore
	@Operation(summary = "邮箱验证码重置密码")
	@PostMapping("/sys/base/login/password/email/reset")
	public Result<String> resetPasswordByEmail(@Validated @RequestBody EmailPasswordResetForm form) {
		service.resetPasswordByEmail(form);
		return Result.success();
	}

	@Operation(summary = "创建滑块验证码", description = "创建一次性登录滑块挑战")
	@PostMapping("/sys/base/captcha/challenge")
	@SaIgnore
	public Result<CaptchaChallengeVO> captchaChallenge(@Validated @RequestBody CaptchaChallengeForm form) {
		return Result.success(service.createCaptchaChallenge(form));
	}

	@Operation(summary = "校验滑块验证码", description = "校验并消费挑战，成功后签发一次性登录票据")
	@PostMapping("/sys/base/captcha/verify")
	@SaIgnore
	public Result<CaptchaTicketVO> captchaVerify(@Validated @RequestBody CaptchaVerifyForm form) {
		return Result.success(service.verifyCaptcha(form));
	}

	@Operation(summary = "用户登出", description = "退出当前登录")
	@PostMapping("/sys/base/logout")
	public Result<String> logout() {
		service.logout();
		return Result.success();
	}
}
