package sm.domain.sys.base.role.controller;

import sm.domain.sys.base.role.model.vo.RoleDefaultVO;
import sm.domain.sys.base.role.model.form.RoleDefaultSaveForm;
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
import sm.domain.sys.base.role.model.form.RoleDataScopeAssignForm;
import sm.domain.sys.base.role.model.vo.RoleDataScopeWorkspaceVO;
import sm.domain.sys.base.role.service.RoleService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

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

    @GetMapping("/sys/base/role/defaultRole")
    @SaCheckPermission(RolePermission.LIST)
    @Operation(summary = "默认角色配置", description = "读取全局默认角色及配置并发版本")
    public Result<RoleDefaultVO> defaultRole() {
        return Result.success(service.defaultRole());
    }

    @PostMapping("/sys/base/role/setDefault")
    @SaCheckPermission(RolePermission.SET_DEFAULT)
    @Operation(summary = "设置默认角色", description = "设置、切换或取消全局默认角色")
    public Result<String> setDefault(@RequestBody @Valid RoleDefaultSaveForm form) {
        service.setDefault(form);
        return Result.success();
    }


	@Operation(summary = "角色列表", description = "获取角色分页列表数据")
	@PostMapping("/sys/base/role/listPage")
	@SaCheckPermission(RolePermission.LIST)
	public Result<PageData<RoleListVO>> listPage(@RequestBody RoleListForm form) {
		return Result.success(service.listPage(form));
	}

	@Operation(summary = "角色选择", description = "基础资料选择：获取角色分页列表数据")
	@PostMapping("/sys/base/role/select")
	@SaCheckPermission(RolePermission.SELECT)
	public Result<PageData<RoleSelectVO>> select(@RequestBody @Valid RoleSelectForm form) {
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

    @Operation(summary = "批量删除角色", description = "同一事务删除整批角色，包含默认角色或任一目标失败时整体回滚")
    @PostMapping("/sys/base/role/deleteBatch")
    @SaCheckPermission(RolePermission.DELETE)
    public Result<String> deleteBatch(@RequestBody @Valid IdsForm form) {
        service.deleteByIds(form.getIds());
        return Result.success();
    }

	@Operation(summary = "分配角色权限", description = "整体替换指定角色的权限关系")
	@PostMapping("/sys/base/role/assignPermissions")
	@SaCheckPermission(RolePermission.ASSIGN_PERMISSIONS)
	public Result<String> assignPermissions(@RequestBody @Valid RolePermissionAssignForm form) {
		service.assignPermissions(form);
		return Result.success();
	}

	@PostMapping("/sys/base/role/dataScopeWorkspace")
	@SaCheckPermission(RolePermission.ASSIGN_DATA_SCOPES)
	public Result<RoleDataScopeWorkspaceVO> dataScopeWorkspace(@RequestBody @Valid IdForm form) {
		return Result.success(service.dataScopeWorkspace(form.getId()));
	}

	@PostMapping("/sys/base/role/assignDataScopes")
	@SaCheckPermission(RolePermission.ASSIGN_DATA_SCOPES)
	public Result<String> assignDataScopes(@RequestBody @Valid RoleDataScopeAssignForm form) {
		service.assignDataScopes(form);
		return Result.success();
	}
}
