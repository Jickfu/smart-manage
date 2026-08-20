package sm.domain.sys.base.app.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.app.constant.AppPermission;
import sm.domain.sys.base.app.model.form.AppListForm;
import sm.domain.sys.base.app.model.form.AppOpenByNumberForm;
import sm.domain.sys.base.app.model.form.AppSaveForm;
import sm.domain.sys.base.app.model.vo.*;
import sm.domain.sys.base.app.service.AppService;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

/**
 * @author Chekfu
 */
@RestController
@Tag(name = "系统管理-应用管理", description = "应用信息管理接口")
@RequiredArgsConstructor
public class AppController {
	private final CurrentUserContext currentUserContext;
	private final AppService service;

	@Operation(summary = "应用列表", description = "获取应用分页列表数据")
	@PostMapping("/sys/base/app/listPage")
	@SaCheckPermission(AppPermission.LIST)
	public Result<PageData<AppListVO>> listPage(@RequestBody AppListForm form) {
		return Result.success(service.listPage(form));
	}

	@Operation(summary = "应用详情", description = "按ID查询应用")
	@PostMapping("/sys/base/app/detail")
	@SaCheckPermission(AppPermission.DETAIL)
	public Result<AppDetailVO> detail(@RequestBody @Valid IdForm form) {
		return Result.success(service.detail(form.getId()));
	}

	@Operation(summary = "保存应用", description = "新增或更新应用")
	@PostMapping("/sys/base/app/save")
	@SaCheckPermission(AppPermission.SAVE)
	public Result<Long> save(@Valid @RequestBody AppSaveForm form) {
		return Result.success(service.save(form));
	}

	@GetMapping("/sys/base/app/createNewData")
	@Operation(summary = "获取新增默认值", description = "获取应用新增时的默认初始数据")
	@SaCheckPermission(AppPermission.SAVE)
	public Result<AppCreateNewDataVO> createNewData() {
		return Result.success(service.createNewData());
	}

	@Operation(summary = "删除应用", description = "按ID删除应用")
	@PostMapping("/sys/base/app/delete")
	@SaCheckPermission(AppPermission.DELETE)
	public Result<String> delete(@RequestBody @Valid IdForm form) {
		service.deleteById(form.getId());
		return Result.success();
	}

	@PostMapping("/sys/base/app/enable")
	@SaCheckPermission(AppPermission.ENABLE)
	public Result<String> enable(@RequestBody @Valid IdsForm form) {
		service.enable(form.getIds());
		return Result.success();
	}

	@PostMapping("/sys/base/app/disable")
	@SaCheckPermission(AppPermission.DISABLE)
	public Result<String> disable(@RequestBody @Valid IdsForm form) {
		service.disable(form.getIds());
		return Result.success();
	}

	@Operation(summary = "领域与应用列表", description = "获取领域及其下应用")
	@GetMapping("/sys/base/app/apps")
	public Result<List<DomainAppsVO>> apps() {
		return Result.success(service.getUserDomainApps(currentUserContext.getUserId()));
	}

	@Operation(summary = "领域与应用列表（全量）", description = "获取所有领域及其下应用（不按用户权限过滤）")
	@GetMapping("/sys/base/app/appsAll")
	@SaCheckPermission(AppPermission.LIST)
	public Result<List<DomainAppsVO>> appsAll() {
		return Result.success(service.getAllDomainApps());
	}

	@Operation(summary = "按应用编号打开应用", description = "返回当前用户有权限访问的应用信息")
	@PostMapping("/sys/base/app/openByNumber")
	public Result<AppVO> openByNumber(@RequestBody @Valid AppOpenByNumberForm form) {
		return Result.success(service.getUserAppByNumber(currentUserContext.getUserId(), form.getNumber()));
	}
}
