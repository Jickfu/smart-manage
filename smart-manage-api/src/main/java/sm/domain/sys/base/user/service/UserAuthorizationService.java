package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.domain.sys.base.user.model.vo.UserAssignedRoleVO;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.domain.sys.base.user.model.vo.UserRoleAssignmentWorkspaceVO;
import sm.domain.sys.base.user.model.vo.UserRoleOrganizationVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 用户角色分配与当前授权查询服务。 */
@Service
@RequiredArgsConstructor
public class UserAuthorizationService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final UserTxService txService;
    private final PermissionService permissionService;
    private final AuthorizationStateHelper authorizationStateHelper;
    private final CurrentUserContext currentUserContext;

    public UserRoleAssignmentWorkspaceVO roleAssignmentWorkspace(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        List<UserAssignmentVO> assignments = loadAssignments(userId);
        Map<Long, List<UserAssignedRoleVO>> rolesByOrgId = userMapper.selectAssignedRoles(userId).stream()
                .collect(Collectors.groupingBy(UserAssignedRoleVO::getOrgId));
        List<UserRoleOrganizationVO> organizations = new ArrayList<>();
        for (UserAssignmentVO assignment : assignments) {
            UserRoleOrganizationVO organization = new UserRoleOrganizationVO();
            organization.setOrg(assignment.getOrg());
            organization.setOrgNamePath(assignment.getOrgNamePath());
            organization.setPosition(assignment.getPosition());
            organization.setIsPrimary(assignment.getIsPrimary());
            organization.setRoles(rolesByOrgId.getOrDefault(assignment.getOrg().getId(), List.of()));
            organizations.add(organization);
        }
        UserRoleAssignmentWorkspaceVO result = new UserRoleAssignmentWorkspaceVO();
        result.setId(user.getId());
        result.setName(user.getName());
        result.setUsername(user.getUsername());
        result.setNumber(user.getNumber());
        result.setOrganizations(organizations);
        return result;
    }

    @BizLog("分配用户角色")
    public void saveRoleAssignment(UserRoleAssignmentSaveForm form) {
        LinkedHashSet<Long> affectedOrgIds = new LinkedHashSet<>(
                userRoleMapper.selectOrgIdsByUserId(form.getUserId()));
        form.getAssignments().forEach(assignment -> affectedOrgIds.add(assignment.getOrgId()));
        txService.saveRoleAssignment(form);
        affectedOrgIds.forEach(orgId ->
                authorizationStateHelper.refreshUserAuthorization(form.getUserId(), orgId));
    }

    public List<String> permissions(String prefix) {
        if (currentUserContext.isAdministrator()) return List.of("*");
        return permissionService.getUserPermissionsByPrefix(
                currentUserContext.getUserId(), currentUserContext.getOrgId(), prefix);
    }

    private List<UserAssignmentVO> loadAssignments(Long userId) {
        List<UserAssignmentEntity> assignments = userAssignmentMapper.selectList(
                new LambdaQueryWrapper<UserAssignmentEntity>()
                        .eq(UserAssignmentEntity::getUserId, userId)
                        .orderByDesc(UserAssignmentEntity::getIsPrimary)
                        .orderByAsc(UserAssignmentEntity::getOrgId));
        Set<Long> orgIds = assignments.stream().map(UserAssignmentEntity::getOrgId).collect(Collectors.toSet());
        Map<Long, OrgReference> orgById = orgReferenceReader.findByIds(orgIds);
        List<UserAssignmentVO> result = new ArrayList<>();
        for (UserAssignmentEntity assignment : assignments) {
            OrgReference org = orgById.get(assignment.getOrgId());
            if (org == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户任职关联了无效组织");
            UserAssignmentVO item = new UserAssignmentVO();
            item.setId(assignment.getId());
            item.setOrg(new ReferenceVO(org.id(), org.number(), org.name()));
            item.setOrgNamePath(org.namePath());
            item.setPosition(assignment.getPosition());
            item.setIsOrgLeader(assignment.getIsOrgLeader());
            item.setIsPrimary(assignment.getIsPrimary());
            result.add(item);
        }
        result.sort(Comparator
                .comparing((UserAssignmentVO assignment) -> !Boolean.TRUE.equals(assignment.getIsPrimary()))
                .thenComparing(UserAssignmentVO::getOrgNamePath));
        return result;
    }
}
