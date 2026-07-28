package sm.domain.sys.monitor.loginlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.loginlog.model.form.LoginLogListForm;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogListVO;
import sm.domain.sys.monitor.loginlog.service.LoginLogQueryService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@Tag(name = "系统服务-登录日志", description = "登录/登出日志查询")
@RequiredArgsConstructor
public class LoginLogController {
	private final LoginLogQueryService loginLogQueryService;

	@PostMapping("/sys/log/login/listPage")
	@Operation(summary = "登录日志分页")
	@SaCheckPermission("sys:log:login:listPage")
	public Result<PageData<LoginLogListVO>> listPage(@Valid @RequestBody LoginLogListForm form) {
		return Result.success(loginLogQueryService.listPage(form));
	}

	@PostMapping("/sys/log/login/detail")
	@Operation(summary = "登录日志详情")
	@SaCheckPermission("sys:log:login:detail")
	public Result<LoginLogListVO> detail(@Valid @RequestBody IdForm form) {
		return Result.success(loginLogQueryService.detail(form.getId()));
	}
}

