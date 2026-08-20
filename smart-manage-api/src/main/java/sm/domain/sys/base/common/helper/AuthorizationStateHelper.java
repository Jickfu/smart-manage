package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.anno.CacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.system.helper.CacheHelper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import sm.system.auth.SessionTerminationContext;
import sm.system.auth.SessionTerminationReason;

/**
 * 授权状态刷新组件。关系或启用状态变化后，使旧会话立即失效，避免继续使用历史权限。
 */
@Component
@RequiredArgsConstructor
public class AuthorizationStateHelper {
	private final CacheHelper cacheHelper;
	private final UserRoleMapper userRoleMapper;

	/** 只刷新共享授权缓存，保留现有登录会话。 */
	public void refreshUsers(Collection<Long> userIds) {
		for (Long userId : new LinkedHashSet<>(userIds)) {
			cacheHelper.<Long, Object>getCache(BaseCacheName.USER_INFO, CacheType.REMOTE, 3600).remove(userId);
			userRoleMapper.selectOrgIdsByUserId(userId).stream()
					.forEach(orgId -> refreshUserAuthorization(userId, orgId));
		}
	}

	/** 精确刷新用户在指定组织下的角色与权限快照。 */
	public void refreshUserAuthorization(Long userId, Long orgId) {
		cacheHelper.<String, Object>getCache(BaseCacheName.USER_AUTHORIZATION, CacheType.REMOTE, 1800)
				.remove(userId + ":" + orgId);
	}

	/** 安全事件先刷新授权缓存，再终止用户的全部会话。 */
	public void terminateUsers(Collection<Long> userIds) {
		terminateUsers(userIds, SessionTerminationReason.SESSION_KICKED);
	}

	public void terminateUsers(Collection<Long> userIds, SessionTerminationReason reason) {
		LinkedHashSet<Long> distinctUserIds = new LinkedHashSet<>(userIds);
		refreshUsers(distinctUserIds);
		for (Long userId : distinctUserIds) {
			SessionTerminationContext.run(reason, () -> StpUtil.logout(userId));
		}
	}

	public void refreshRoleUsers(Long roleId) {
		refreshScopes(roleUserScopes(roleId));
	}

	public List<UserRoleEntity> roleUserScopes(Long roleId) {
		return userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
				.select(UserRoleEntity::getUserId, UserRoleEntity::getOrgId)
				.eq(UserRoleEntity::getRoleId, roleId));
	}

	public List<UserRoleEntity> permissionUserScopes(Long permissionId) {
		return userRoleMapper.selectByPermissionId(permissionId);
	}

	public void refreshScopes(Collection<UserRoleEntity> relations) {
		for (UserRoleEntity relation : relations) {
			refreshUserAuthorization(relation.getUserId(), relation.getOrgId());
		}
	}
}
