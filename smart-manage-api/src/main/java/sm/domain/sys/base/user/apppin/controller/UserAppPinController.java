package sm.domain.sys.base.user.apppin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.user.apppin.model.form.UserAppPinForm;
import sm.domain.sys.base.user.apppin.model.vo.PinnedAppVO;
import sm.domain.sys.base.user.apppin.service.UserAppPinService;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "用户固定应用", description = "当前登录用户的应用固定配置")
public class UserAppPinController {
	private final UserAppPinService service;

	@GetMapping("/sys/base/user/current/app-pins")
	@Operation(summary = "固定应用列表")
	public Result<List<PinnedAppVO>> list() {
		return Result.success(service.listCurrentUserPins());
	}

	@PostMapping("/sys/base/user/current/app-pins/pin")
	@Operation(summary = "固定应用")
	public Result<String> pin(@RequestBody @Valid UserAppPinForm form) {
		service.pin(form.getAppNumber());
		return Result.success();
	}

	@PostMapping("/sys/base/user/current/app-pins/unpin")
	@Operation(summary = "取消固定应用")
	public Result<String> unpin(@RequestBody @Valid UserAppPinForm form) {
		service.unpin(form.getAppNumber());
		return Result.success();
	}
}
