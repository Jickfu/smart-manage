package sm.domain.sys.base.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.role.constant.RolePermission;
import sm.domain.sys.base.role.model.form.RoleListForm;
import sm.domain.sys.base.role.model.form.RoleSaveForm;
import sm.domain.sys.base.role.model.form.RoleSelectForm;
import sm.domain.sys.base.role.model.form.RolePermissionAssignForm;
import sm.domain.sys.base.role.model.vo.RoleCreateNewDataVO;
import sm.domain.sys.base.role.model.vo.RoleDetailVO;
import sm.domain.sys.base.role.model.vo.RoleListVO;
import sm.domain.sys.base.role.model.vo.RoleSelectVO;
import sm.domain.sys.base.role.service.RoleService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

/**
 * 角色管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "角色管理", description = "角色信息管理接口")
@RequiredArgsConstructor
public class RoleController {
	private final RoleService service;

	@Operation(summary = "角色列表", description = "获取角色分页列表数据")
	@PostMapping("/sys/base/role/listPage")
	@SaCheckPermission(RolePermission.LIST)
	public Result<PageData<RoleListVO>> listPage(@RequestBody RoleListForm form) {
		return Result.success(service.listPage(form));
	}

	@Operation(summary = "角色全量列表", description = "获取用户角色分配所需的全部角色轻量数据")
	@PostMapping("/sys/base/role/listAll")
	@SaCheckPermission(RolePermission.LIST)
	public Result<List<RoleSelectVO>> listAll() {
		return Result.success(service.listAll());
	}

	@Operation(summary = "角色选择", description = "基础资料选择：获取角色分页列表数据")
	@PostMapping("/sys/base/role/select")
	@SaCheckPermission(RolePermission.SELECT)
	public Result<PageData<RoleSelectVO>> select(@RequestBody RoleSelectForm form) {
		return Result.success(service.select(form));
	}

	@Operation(summary = "角色详情", description = "根据ID获取角色详情")
	@SaCheckPermission(RolePermission.DETAIL)
	@PostMapping("/sys/base/role/detail")
	public Result<RoleDetailVO> detail(@RequestBody @Valid IdForm form) {
		return Result.success(service.detail(form.getId()));
	}

	@Operation(summary = "保存角色", description = "新增或更新角色")
	@PostMapping("/sys/base/role/save")
	@SaCheckPermission(RolePermission.SAVE)
	public Result<Long> save(@Valid @RequestBody RoleSaveForm form) {
		return Result.success(service.save(form));
	}

	@GetMapping("/sys/base/role/createNewData")
	@Operation(summary = "获取新增默认值", description = "获取角色新增时的默认初始数据")
	@SaCheckPermission(RolePermission.SAVE)
	public Result<RoleCreateNewDataVO> createNewData() {
		return Result.success(service.createNewData());
	}

	@Operation(summary = "删除角色", description = "根据ID删除角色")
	@PostMapping("/sys/base/role/delete")
	@SaCheckPermission(RolePermission.DELETE)
	public Result<String> delete(@RequestBody @Valid IdForm form) {
		service.deleteById(form.getId());
		return Result.success();
	}

	@Operation(summary = "分配角色权限", description = "整体替换指定角色的权限关系")
	@PostMapping("/sys/base/role/assignPermissions")
	@SaCheckPermission(RolePermission.ASSIGN_PERMISSIONS)
	public Result<String> assignPermissions(@RequestBody @Valid RolePermissionAssignForm form) {
		service.assignPermissions(form);
		return Result.success();
	}
}
