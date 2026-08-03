package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.common.service.CurrentUserService;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.role.service.RoleService;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author Chekfu
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
	private final CurrentUserContext currentUserContext;
	private final CurrentUserService currentUserService;
	private final PermissionService permissionService;
	private final RoleService roleService;

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		if (currentUserService.isAdministrator()) {
			return List.of("*");
		}
		long uid = Long.parseLong(String.valueOf(loginId));
		Long orgId = currentUserContext.getOrgId();
		return permissionService.getUserPermissions(uid, orgId);
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		if (currentUserService.isAdministrator()) {
			return List.of("administrator");
		}
		long userId = Long.parseLong(String.valueOf(loginId));
		return roleService.getUserRoleNumbers(userId, currentUserContext.getOrgId());
	}
}
