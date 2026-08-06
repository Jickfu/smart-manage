package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.anno.CacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.helper.CacheHelper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collection;
import java.util.LinkedHashSet;
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
			cacheHelper.<Long, Object>getCache(CacheConstant.USER_INFO, CacheType.REMOTE).remove(userId);
		}
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
		refreshUsers(userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
				.select(UserRoleEntity::getUserId)
				.eq(UserRoleEntity::getRoleId, roleId))
				.stream()
				.map(UserRoleEntity::getUserId)
				.toList());
	}
}
