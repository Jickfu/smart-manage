package sm.domain.sys.base.common.helper;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.common.model.vo.UserAuthorizationVO;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.role.mapper.RoleMapper;

import java.util.concurrent.TimeUnit;

/** 独立缓存访问器，确保授权查询始终经过 JetCache 代理。 */
@Component
@RequiredArgsConstructor
public class UserAuthorizationCacheAccessor {
    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;

    @Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.USER_AUTHORIZATION,
            key = "#userId + ':' + #orgId", expire = 30, timeUnit = TimeUnit.MINUTES)
    public UserAuthorizationVO get(Long userId, Long orgId) {
        return new UserAuthorizationVO(
                roleMapper.selectUserRoleNumbers(userId, orgId),
                permissionMapper.selectUserPermissionNumbers(userId, orgId, null));
    }
}
