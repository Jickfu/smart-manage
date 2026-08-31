package sm.domain.sys.base.datascope.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.datascope.model.DataScopeRuleSnapshot;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeOrgEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 数据范围配置的公开业务边界。 */
@Service
@RequiredArgsConstructor
public class DataScopeConfigurationService {
    private final RoleDataScopeMapper ruleMapper;
    private final RoleDataScopeOrgMapper ruleOrgMapper;
    private final DataScopeConfigurationTxService txService;

    public List<DataScopeRuleSnapshot> roleRules(Long roleId) {
        List<RoleDataScopeEntity> rules = ruleMapper.selectList(new LambdaQueryWrapper<RoleDataScopeEntity>()
                .eq(RoleDataScopeEntity::getRoleId, roleId)
                .orderByAsc(RoleDataScopeEntity::getResourceType, RoleDataScopeEntity::getAction));
        List<Long> ruleIds = rules.stream().map(RoleDataScopeEntity::getId).toList();
        Map<Long, List<Long>> orgIdsByRule = ruleIds.isEmpty() ? Map.of()
                : ruleOrgMapper.selectList(new LambdaQueryWrapper<RoleDataScopeOrgEntity>()
                        .in(RoleDataScopeOrgEntity::getScopeRuleId, ruleIds)).stream()
                .collect(Collectors.groupingBy(RoleDataScopeOrgEntity::getScopeRuleId,
                        Collectors.mapping(RoleDataScopeOrgEntity::getOrgId, Collectors.toList())));
        return rules.stream().map(rule -> new DataScopeRuleSnapshot(rule.getResourceType(), rule.getAction(),
                rule.getScopeType(), orgIdsByRule.getOrDefault(rule.getId(), List.of()))).toList();
    }

    public void replaceRoleRules(Long roleId, List<DataScopeRuleSnapshot> rules) {
        txService.replaceRoleRules(roleId, rules);
    }
}
