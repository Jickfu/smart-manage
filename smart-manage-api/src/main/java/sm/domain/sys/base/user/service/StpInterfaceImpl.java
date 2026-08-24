package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.common.helper.UserAuthorizationCacheAccessor;

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
	private final UserAuthorizationCacheAccessor authorizationCacheAccessor;

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		if (currentUserContext.isAdministrator()) {
			return List.of("*");
		}
		long uid = Long.parseLong(String.valueOf(loginId));
		Long orgId = currentUserContext.getOrgId();
		return authorizationCacheAccessor.get(uid, orgId).getPermissionNumbers();
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		if (currentUserContext.isAdministrator()) {
			return List.of("administrator");
		}
		long userId = Long.parseLong(String.valueOf(loginId));
		return authorizationCacheAccessor.get(userId, currentUserContext.getOrgId()).getRoleNumbers();
	}
}
