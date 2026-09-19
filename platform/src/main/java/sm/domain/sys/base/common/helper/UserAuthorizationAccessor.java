package sm.domain.sys.base.common.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.model.vo.UserAuthorizationVO;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.role.mapper.RoleMapper;

/** 授权决策只读取数据库，撤权不依赖 Redis 删除或缓存回填时序。 */
@Component
@RequiredArgsConstructor
public class UserAuthorizationAccessor {
    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;

    public UserAuthorizationVO get(Long userId, Long orgId) {
        return new UserAuthorizationVO(
                roleMapper.selectUserRoleNumbers(userId, orgId),
                permissionMapper.selectUserPermissionNumbers(userId, orgId, null));
    }
}
