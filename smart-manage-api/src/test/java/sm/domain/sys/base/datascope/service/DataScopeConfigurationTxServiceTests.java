package sm.domain.sys.base.datascope.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.datascope.model.DataScopeRuleSnapshot;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeOrgEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataScopeConfigurationTxServiceTests {
    @Test
    void customOrganizationRelationsHaveSetSemantics() {
        RoleDataScopeMapper ruleMapper = mock(RoleDataScopeMapper.class);
        RoleDataScopeOrgMapper orgMapper = mock(RoleDataScopeOrgMapper.class);
        when(ruleMapper.selectList(any())).thenReturn(List.of());
        when(ruleMapper.insert(any(RoleDataScopeEntity.class))).thenAnswer(invocation -> {
            RoleDataScopeEntity rule = invocation.getArgument(0);
            rule.setId(100L);
            return 1;
        });
        when(orgMapper.insert(any(RoleDataScopeOrgEntity.class))).thenReturn(1);

        new DataScopeConfigurationTxService(ruleMapper, orgMapper).replaceRoleRules(10L,
                List.of(new DataScopeRuleSnapshot("purchase", null, "CUSTOM_ORGS", List.of(12L, 11L, 12L))));

        ArgumentCaptor<RoleDataScopeOrgEntity> relations = ArgumentCaptor.forClass(RoleDataScopeOrgEntity.class);
        verify(orgMapper, times(2)).insert(relations.capture());
        assertEquals(List.of(12L, 11L), relations.getAllValues().stream().map(RoleDataScopeOrgEntity::getOrgId).toList());
        assertEquals(List.of(100L, 100L), relations.getAllValues().stream().map(RoleDataScopeOrgEntity::getScopeRuleId).toList());
    }
}
