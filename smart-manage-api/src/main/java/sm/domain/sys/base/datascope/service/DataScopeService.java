package sm.domain.sys.base.datascope.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.system.datascope.DataScope;
import sm.domain.sys.base.datascope.model.DataScopeType;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeOrgEntity;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.system.datascope.DataScopeResolver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 解析角色默认、资源默认和操作覆盖，并将多个角色的 Allow 结果合并。 */
@Service
@RequiredArgsConstructor
public class DataScopeService implements DataScopeResolver {
    private final CurrentUserContext currentUserContext;
    private final RoleDataScopeMapper ruleMapper;
    private final RoleDataScopeOrgMapper ruleOrgMapper;
    private final OrgMapper orgMapper;

    @Override
    public DataScope resolve(String resourceType, String action) {
        Long userId = currentUserContext.getUserId();
        if (currentUserContext.isAdministrator()) {
            return new DataScope(true, true, Set.of(), userId);
        }
        Long currentOrgId = currentUserContext.getOrgId();
        List<RoleDataScopeEntity> rules = ruleMapper.selectEffectiveRules(userId, currentOrgId, resourceType, action);
        boolean selfIncluded = false;
        Set<Long> allowedOrgIds = new HashSet<>();
        Map<Long, Set<Long>> customOrgIdsByRule = customOrgIdsByRule(rules);
        for (RoleDataScopeEntity rule : rules) {
            DataScopeType scopeType = DataScopeType.valueOf(rule.getScopeType());
            if (scopeType == DataScopeType.ALL) return new DataScope(true, true, Set.of(), userId);
            if (scopeType == DataScopeType.SELF) selfIncluded = true;
            if (scopeType == DataScopeType.ORG) allowedOrgIds.add(currentOrgId);
            if (scopeType == DataScopeType.ORG_AND_CHILDREN) allowedOrgIds.addAll(orgAndChildren(currentOrgId));
            if (scopeType == DataScopeType.CUSTOM_ORGS) {
                allowedOrgIds.addAll(customOrgIdsByRule.getOrDefault(rule.getId(), Set.of()));
            }
        }
        return new DataScope(false, selfIncluded, allowedOrgIds, userId);
    }

    private Map<Long, Set<Long>> customOrgIdsByRule(List<RoleDataScopeEntity> rules) {
        List<Long> ruleIds = rules.stream().map(RoleDataScopeEntity::getId).filter(id -> id != null && id > 0).toList();
        Map<Long, Set<Long>> result = new HashMap<>();
        if (ruleIds.isEmpty()) return result;
        for (RoleDataScopeOrgEntity relation : ruleOrgMapper.selectList(
                new LambdaQueryWrapper<RoleDataScopeOrgEntity>().in(RoleDataScopeOrgEntity::getScopeRuleId, ruleIds))) {
            result.computeIfAbsent(relation.getScopeRuleId(), ignored -> new HashSet<>()).add(relation.getOrgId());
        }
        return result;
    }

    private Set<Long> orgAndChildren(Long rootOrgId) {
        List<OrgEntity> organizations = orgMapper.selectList(new LambdaQueryWrapper<OrgEntity>()
                .select(OrgEntity::getId, OrgEntity::getParentId));
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (OrgEntity organization : organizations) {
            childrenByParent.computeIfAbsent(organization.getParentId(), ignored -> new ArrayList<>())
                    .add(organization.getId());
        }
        Set<Long> result = new HashSet<>();
        ArrayDeque<Long> pending = new ArrayDeque<>();
        pending.add(rootOrgId);
        while (!pending.isEmpty()) {
            Long orgId = pending.removeFirst();
            if (result.add(orgId)) pending.addAll(childrenByParent.getOrDefault(orgId, List.of()));
        }
        return result;
    }
}
