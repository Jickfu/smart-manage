package sm.domain.sys.base.role.service;

import sm.domain.sys.base.role.converter.RoleConverterImpl;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.datascope.service.DataScopeConfigurationService;
import sm.system.resource.BusinessResourceRegistry;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleDataScopeAssignForm;
import sm.domain.sys.base.role.model.form.RoleDataScopeRuleForm;
import sm.domain.sys.base.role.model.form.RoleSelectForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceTests {

    @Test
    void selectIncludesDescriptionForRoleAssignment() {
        RoleMapper mapper = mock(RoleMapper.class);
        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setNumber("reviewer");
        role.setName("审核角色");
        role.setDescription("负责审核业务单据");
        Page<RoleEntity> page = new Page<>(1, 20);
        page.setTotal(1);
        page.setRecords(List.of(role));
        when(mapper.selectPage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);
        RoleService service = new RoleService(
                mapper,
                mock(RolePermissionMapper.class),
                mock(RoleTxService.class),
                new RoleConverterImpl(), mock(DataScopeConfigurationService.class),
                mock(BusinessResourceRegistry.class));

        RoleSelectForm form = new RoleSelectForm();
        form.setPageNum(1);
        form.setPageSize(20);
        form.setKeyword("审核");
        form.setExcludedIds(List.of(2L));
        var result = service.select(form);

        assertEquals(1, result.getRecords().size());
        assertEquals("负责审核业务单据", result.getRecords().getFirst().getDescription());
    }

    @Test
    void roleNumbersAreLoadedByUserAndOrganization() {
        RoleMapper mapper = mock(RoleMapper.class);
        when(mapper.selectUserRoleNumbers(10L, 20L)).thenReturn(List.of("operator", "reviewer"));
        RoleService service = new RoleService(
                mapper,
                mock(RolePermissionMapper.class),
                mock(RoleTxService.class),
                new RoleConverterImpl(), mock(DataScopeConfigurationService.class),
                mock(BusinessResourceRegistry.class));

        assertEquals(List.of("operator", "reviewer"), service.getUserRoleNumbers(10L, 20L));
    }

    @Test
    void assignDataScopesRejectsUndeclaredActionBeforeTransaction() {
        RoleTxService txService = mock(RoleTxService.class);
        BusinessResourceRegistry resourceRegistry = mock(BusinessResourceRegistry.class);
        doThrow(new BizException(ResultEnum.PERMISSION_ERROR, "业务资源未声明该数据权限操作"))
                .when(resourceRegistry).requireDataScopeAction("scm.procurement.purchase-requisition", "UNKNOWN");
        RoleService service = service(txService, resourceRegistry);
        RoleDataScopeAssignForm form = dataScopeForm("UNKNOWN");

        assertThrows(BizException.class, () -> service.assignDataScopes(form));

        verify(resourceRegistry).dataScopeActions("scm.procurement.purchase-requisition");
        verify(resourceRegistry).requireDataScopeAction("scm.procurement.purchase-requisition", "UNKNOWN");
        verify(txService, never()).assignDataScopes(form);
    }

    @Test
    void assignResourceDefaultStillValidatesDataScopeSupport() {
        RoleTxService txService = mock(RoleTxService.class);
        BusinessResourceRegistry resourceRegistry = mock(BusinessResourceRegistry.class);
        RoleService service = service(txService, resourceRegistry);
        RoleDataScopeAssignForm form = dataScopeForm(null);

        service.assignDataScopes(form);

        verify(resourceRegistry).dataScopeActions("scm.procurement.purchase-requisition");
        verify(resourceRegistry, never()).requireDataScopeAction("scm.procurement.purchase-requisition", null);
        verify(txService).assignDataScopes(form);
    }

    private RoleService service(RoleTxService txService, BusinessResourceRegistry resourceRegistry) {
        return new RoleService(
                mock(RoleMapper.class),
                mock(RolePermissionMapper.class),
                txService,
                new RoleConverterImpl(), mock(DataScopeConfigurationService.class),
                resourceRegistry);
    }

    private RoleDataScopeAssignForm dataScopeForm(String action) {
        RoleDataScopeRuleForm rule = new RoleDataScopeRuleForm();
        rule.setResourceType("scm.procurement.purchase-requisition");
        rule.setAction(action);
        rule.setScopeType("ORG");
        RoleDataScopeAssignForm form = new RoleDataScopeAssignForm();
        form.setRoleId(10L);
        form.setVersion(1);
        form.setDefaultDataScope("SELF");
        form.setRules(List.of(rule));
        return form;
    }
}
