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

/**
 * 授权状态刷新组件。关系或启用状态变化后，使旧会话立即失效，避免继续使用历史权限。
 */
@Component
@RequiredArgsConstructor
public class AuthorizationStateHelper {
	private final CacheHelper cacheHelper;
	private final UserRoleMapper userRoleMapper;

	public void invalidateUsers(Collection<Long> userIds) {
		for (Long userId : userIds.stream().distinct().toList()) {
			cacheHelper.<Long, Object>getCache(CacheConstant.USER_INFO, CacheType.REMOTE).remove(userId);
			StpUtil.logout(userId);
		}
	}

	public void invalidateRoleUsers(Long roleId) {
		invalidateUsers(userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
				.select(UserRoleEntity::getUserId)
				.eq(UserRoleEntity::getRoleId, roleId))
				.stream()
				.map(UserRoleEntity::getUserId)
				.toList());
	}
}
