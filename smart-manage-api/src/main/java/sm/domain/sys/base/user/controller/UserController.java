package sm.domain.sys.base.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.user.constant.UserPermission;
import sm.domain.sys.base.user.model.form.UserListForm;
import sm.domain.sys.base.user.model.form.UserPermissionsForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignForm;
import sm.domain.sys.base.user.model.form.CurrentUserThemeForm;
import sm.domain.sys.base.user.model.vo.UserCreateNewDataVO;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.domain.sys.base.user.service.UserService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;

/**
 * 用户管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "用户管理", description = "用户信息管理接口")
@RequiredArgsConstructor
public class UserController {
	private final UserService service;
	private final FileStorageServiceFactory storageFactory;

	@GetMapping("/sys/base/user/avatar/{userId}")
	public ResponseEntity<StreamingResponseBody> avatar(@PathVariable Long userId) {
		AttachmentEntity attachment = service.requireAvatar(userId);
		FileStorageService storage = storageFactory.getService(attachment.getStorageType());
		StreamingResponseBody body = outputStream -> {
			try (java.io.InputStream inputStream = storage.openStream(attachment.getObjectKey())) {
				inputStream.transferTo(outputStream);
			}
		};
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(attachment.getMimeType()))
				.contentLength(attachment.getFileSize())
				.header("X-Content-Type-Options", "nosniff")
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
				.body(body);
	}

	@PostMapping("/sys/base/user/listPage")
	@Operation(summary = "用户列表", description = "获取用户分页列表数据")
	@SaCheckPermission(UserPermission.LIST)
	public Result<PageData<UserListVO>> listPage(@RequestBody UserListForm form) {
		return Result.success(service.listPage(form));
	}

	@GetMapping("/sys/base/user/current")
	@Operation(summary = "用户信息", description = "获取当前登录用户信息")
	public Result<UserInfoVO> current() {
		return Result.success(service.current());
	}

	@PostMapping("/sys/base/user/current/theme")
	@Operation(summary = "保存个人主题", description = "保存当前登录用户选择的预置主题色")
	public Result<String> updateCurrentTheme(@RequestBody @Valid CurrentUserThemeForm form) {
		service.updateCurrentTheme(form.getThemeColor());
		return Result.success();
	}

	@PostMapping("/sys/base/user/permissions")
	@Operation(summary = "用户权限", description = "按前缀获取当前用户权限编码列表")
	public Result<List<String>> permissions(@RequestBody @Valid UserPermissionsForm form) {
		return Result.success(service.permissions(form.getPrefix()));
	}

	@Operation(summary = "用户详情", description = "按ID查询用户")
	@SaCheckPermission(UserPermission.DETAIL)
	@PostMapping("/sys/base/user/detail")
	public Result<UserInfoVO> detail(@RequestBody @Valid IdForm form) {
		return Result.success(service.detail(form.getId()));
	}

	@PostMapping("/sys/base/user/save")
	@Operation(summary = "保存用户", description = "新增或更新用户")
	@SaCheckPermission(UserPermission.SAVE)
	public Result<Long> saveUser(@RequestBody @Valid UserSaveForm form) {
		return Result.success(service.save(form));
	}

	@PostMapping("/sys/base/user/delete")
	@Operation(summary = "删除用户", description = "按ID删除用户")
	@SaCheckPermission(UserPermission.DELETE)
	public Result<String> deleteUser(@RequestBody @Valid IdForm form) {
		service.deleteById(form.getId());
		return Result.success();
	}

	@PostMapping("/sys/base/user/enable")
	@SaCheckPermission(UserPermission.ENABLE)
	public Result<String> enable(@RequestBody @Valid IdsForm form) {
		service.enable(form.getIds());
		return Result.success();
	}

	@PostMapping("/sys/base/user/disable")
	@SaCheckPermission(UserPermission.DISABLE)
	public Result<String> disable(@RequestBody @Valid IdsForm form) {
		service.disable(form.getIds());
		return Result.success();
	}

	@GetMapping("/sys/base/user/createNewData")
	@Operation(summary = "获取新增默认值", description = "获取用户新增时的默认初始数据")
	@SaCheckPermission(UserPermission.SAVE)
	public Result<UserCreateNewDataVO> createNewData() {
		return Result.success(service.createNewData());
	}

	@Operation(summary = "分配用户角色", description = "整体替换指定用户在当前组织下的角色关系")
	@PostMapping("/sys/base/user/assignRoles")
	@SaCheckPermission(UserPermission.ASSIGN_ROLES)
	public Result<String> assignRoles(@RequestBody @Valid UserRoleAssignForm form) {
		service.assignRoles(form);
		return Result.success();
	}

	@Operation(summary = "重置用户密码", description = "生成随机临时密码并要求用户登录后修改")
	@PostMapping("/sys/base/user/resetPassword")
	@SaCheckPermission(UserPermission.RESET_PASSWORD)
	public Result<ResetPasswordVO> resetPassword(@RequestBody @Valid IdForm form) {
		return Result.success(service.resetPassword(form.getId()));
	}
}
