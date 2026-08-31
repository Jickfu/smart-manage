package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.user.contract.UserAssignmentReader;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Collection;
import java.util.List;

/** 用户任职关系的只读引用服务。 */
@Service
@RequiredArgsConstructor
public class UserAssignmentReferenceService implements UserAssignmentReader {

    private final UserAssignmentMapper mapper;
    private final UserMapper userMapper;

    @Override
    public void requireAssignment(Long userId, Long orgId) {
        if (userId == null || orgId == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户和组织不能为空");
        }
        long assignments = mapper.selectCount(new LambdaQueryWrapper<UserAssignmentEntity>()
                .eq(UserAssignmentEntity::getUserId, userId)
                .eq(UserAssignmentEntity::getOrgId, orgId));
        if (assignments == 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户未任职于所选组织");
        }
    }

    @Override
    public boolean hasEnabledPrimaryAssignments(Collection<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) return false;
        List<Long> userIds = mapper.selectList(new LambdaQueryWrapper<UserAssignmentEntity>()
                        .in(UserAssignmentEntity::getOrgId, orgIds)
                        .eq(UserAssignmentEntity::getIsPrimary, true))
                .stream().map(UserAssignmentEntity::getUserId).distinct().toList();
        if (userIds.isEmpty()) return false;
        return userMapper.selectByIds(userIds).stream().anyMatch(user -> Boolean.TRUE.equals(user.getEnabled()));
    }
}
