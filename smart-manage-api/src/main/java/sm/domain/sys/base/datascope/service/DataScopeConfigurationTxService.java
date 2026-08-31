package sm.domain.sys.base.datascope.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.datascope.model.DataScopeRuleSnapshot;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeOrgEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class DataScopeConfigurationTxService {
    private final RoleDataScopeMapper ruleMapper;
    private final RoleDataScopeOrgMapper ruleOrgMapper;

    void replaceRoleRules(Long roleId, List<DataScopeRuleSnapshot> rules) {
        List<RoleDataScopeEntity> oldRules = ruleMapper.selectList(new LambdaQueryWrapper<RoleDataScopeEntity>()
                .eq(RoleDataScopeEntity::getRoleId, roleId));
        if (!oldRules.isEmpty()) {
            ruleOrgMapper.delete(new LambdaQueryWrapper<RoleDataScopeOrgEntity>()
                    .in(RoleDataScopeOrgEntity::getScopeRuleId,
                            oldRules.stream().map(RoleDataScopeEntity::getId).toList()));
        }
        ruleMapper.delete(new LambdaQueryWrapper<RoleDataScopeEntity>()
                .eq(RoleDataScopeEntity::getRoleId, roleId));
        for (DataScopeRuleSnapshot snapshot : rules) {
            RoleDataScopeEntity rule = new RoleDataScopeEntity();
            rule.setRoleId(roleId);
            rule.setResourceType(snapshot.resourceType());
            rule.setAction(snapshot.action());
            rule.setScopeType(snapshot.scopeType());
            if (ruleMapper.insert(rule) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "数据范围规则写入失败");
            }
            for (Long orgId : snapshot.orgIds().stream().distinct().toList()) {
                RoleDataScopeOrgEntity relation = new RoleDataScopeOrgEntity();
                relation.setScopeRuleId(rule.getId());
                relation.setOrgId(orgId);
                if (ruleOrgMapper.insert(relation) != 1) {
                    throw new BizException(ResultEnum.PERSISTENCE_ERROR, "自定义组织写入失败");
                }
            }
        }
    }
}
