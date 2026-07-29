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
import sm.domain.sys.base.login.model.form.LoginForm;
import sm.domain.sys.base.login.model.form.PasswordChangeForm;
import sm.domain.sys.base.login.model.vo.CaptchaVO;
import sm.domain.sys.base.login.model.vo.LoginVO;
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

	@Operation(summary = "用户登录", description = "通过用户名密码登录获取token")
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

	@Operation(summary = "获取验证码", description = "获取登录验证码图片")
	@GetMapping(value = "/sys/base/captcha")
	@SaIgnore
	public Result<CaptchaVO> captcha() throws Exception {
		return Result.success(service.captcha());
	}

	@Operation(summary = "用户登出", description = "退出当前登录")
	@PostMapping("/sys/base/logout")
	public Result<String> logout() {
		service.logout();
		return Result.success();
	}
}
