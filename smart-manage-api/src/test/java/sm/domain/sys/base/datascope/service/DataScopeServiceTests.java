package sm.domain.sys.base.datascope.service;

import org.junit.jupiter.api.Test;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.system.resource.BusinessResourceRegistry;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataScopeServiceTests {
    @Test
    void administratorAlwaysResolvesAll() {
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(1L);
        when(context.isAdministrator()).thenReturn(true);
        DataScopeService service = new DataScopeService(context, mock(BusinessResourceRegistry.class),
                mock(RoleDataScopeMapper.class), mock(RoleDataScopeOrgMapper.class), mock(OrgMapper.class));

        var scope = service.resolve("scm.procurement.purchase-requisition", "VIEW");

        assertTrue(scope.all());
        assertTrue(scope.selfIncluded());
    }

    @Test
    void multipleRoleScopesAreCombinedAsAllowUnion() {
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        when(context.getOrgId()).thenReturn(20L);
        RoleDataScopeMapper mapper = mock(RoleDataScopeMapper.class);
        RoleDataScopeEntity orgRule = rule(-1L, "ORG");
        RoleDataScopeEntity selfRule = rule(-2L, "SELF");
        when(mapper.selectEffectiveRules(10L, 20L, "scm.procurement.purchase-requisition", "VIEW"))
                .thenReturn(List.of(orgRule, selfRule));
        DataScopeService service = new DataScopeService(context, mock(BusinessResourceRegistry.class),
                mapper, mock(RoleDataScopeOrgMapper.class), mock(OrgMapper.class));

        var scope = service.resolve("scm.procurement.purchase-requisition", "VIEW");

        assertFalse(scope.all());
        assertTrue(scope.selfIncluded());
        assertEquals(java.util.Set.of(20L), scope.orgIds());
    }

    private RoleDataScopeEntity rule(Long id, String type) {
        RoleDataScopeEntity rule = new RoleDataScopeEntity();
        rule.setId(id);
        rule.setScopeType(type);
        return rule;
    }
}
