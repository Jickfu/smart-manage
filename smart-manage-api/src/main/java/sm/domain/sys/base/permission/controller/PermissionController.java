package sm.domain.sys.base.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.permission.constant.PermissionManagementPermission;
import sm.domain.sys.base.permission.model.form.PermissionListForm;
import sm.domain.sys.base.permission.model.form.PermissionSaveForm;
import sm.domain.sys.base.permission.model.form.PermissionSelectForm;
import sm.domain.sys.base.permission.model.vo.PermissionCreateNewDataVO;
import sm.domain.sys.base.permission.model.vo.PermissionDetailVO;
import sm.domain.sys.base.permission.model.vo.PermissionListVO;
import sm.domain.sys.base.permission.model.vo.PermissionSelectVO;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

/**
 * 权限管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "权限管理", description = "权限信息管理接口")
@RequiredArgsConstructor
public class PermissionController {
	private final PermissionService service;

	@Operation(summary = "权限列表", description = "获取权限分页列表数据")
	@PostMapping("/sys/base/permission/listPage")
	@SaCheckPermission(PermissionManagementPermission.LIST)
	public Result<PageData<PermissionListVO>> listPage(@RequestBody PermissionListForm form) {
		return Result.success(service.listPage(form));
	}

	@Operation(summary = "权限全量列表", description = "获取角色权限分配所需的全部权限轻量数据")
	@PostMapping("/sys/base/permission/listAll")
	@SaCheckPermission(PermissionManagementPermission.LIST)
	public Result<List<PermissionSelectVO>> listAll() {
		return Result.success(service.listAll());
	}

	@Operation(summary = "权限选择", description = "基础资料选择：获取权限分页列表数据")
	@PostMapping("/sys/base/permission/select")
	@SaCheckPermission(PermissionManagementPermission.SELECT)
	public Result<PageData<PermissionSelectVO>> select(@RequestBody PermissionSelectForm form) {
		return Result.success(service.select(form));
	}

	@Operation(summary = "权限详情", description = "按ID查询权限")
	@SaCheckPermission(PermissionManagementPermission.DETAIL)
	@PostMapping("/sys/base/permission/detail")
	public Result<PermissionDetailVO> detail(@RequestBody @Valid IdForm form) {
		return Result.success(service.detail(form.getId()));
	}

	@PostMapping("/sys/base/permission/save")
	@Operation(summary = "保存权限", description = "新增或更新权限")
	@SaCheckPermission(PermissionManagementPermission.SAVE)
	public Result<Long> save(@Valid @RequestBody PermissionSaveForm form) {
		return Result.success(service.save(form));
	}

	@GetMapping("/sys/base/permission/createNewData")
	@Operation(summary = "获取新增默认值", description = "获取权限新增时的默认初始数据")
	@SaCheckPermission(PermissionManagementPermission.SAVE)
	public Result<PermissionCreateNewDataVO> createNewData() {
		return Result.success(service.createNewData());
	}

	@PostMapping("/sys/base/permission/delete")
	@Operation(summary = "删除权限", description = "按ID删除权限")
	@SaCheckPermission(PermissionManagementPermission.DELETE)
	public Result<String> delete(@RequestBody @Valid IdForm form) {
		service.deleteById(form.getId());
		return Result.success();
	}
}
