package sm.domain.sys.monitor.loginlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.loginlog.constant.LoginLogPermission;
import sm.domain.sys.monitor.loginlog.model.form.LoginLogListForm;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogDetailVO;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogListVO;
import sm.domain.sys.monitor.loginlog.service.LoginLogService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@Tag(name = "系统服务-登录日志", description = "登录/登出日志查询")
@RequiredArgsConstructor
public class LoginLogController {
	private final LoginLogService service;

	@PostMapping("/sys/log/login/listPage")
	@Operation(summary = "登录日志分页")
	@SaCheckPermission(LoginLogPermission.LIST)
	public Result<PageData<LoginLogListVO>> listPage(@Valid @RequestBody LoginLogListForm form) {
		return Result.success(service.listPage(form));
	}

	@PostMapping("/sys/log/login/current/listPage")
	@Operation(summary = "当前账号登录日志分页")
	public Result<PageData<LoginLogListVO>> currentListPage(@Valid @RequestBody LoginLogListForm form) {
		return Result.success(service.listCurrentPage(form, StpUtil.getLoginIdAsLong()));
	}

	@PostMapping("/sys/log/login/detail")
	@Operation(summary = "登录日志详情")
	@SaCheckPermission(LoginLogPermission.DETAIL)
	public Result<LoginLogDetailVO> detail(@Valid @RequestBody IdForm form) {
		return Result.success(service.detail(form.getId()));
	}
}
