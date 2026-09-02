package sm.domain.sys.base.role.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.datascope.service.DataScopeConfigurationService;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleSaveForm;
import sm.domain.sys.base.role.model.form.RoleDataScopeAssignForm;
import sm.domain.sys.base.role.model.form.RoleDataScopeRuleForm;
import sm.domain.sys.base.datascope.model.DataScopeRuleSnapshot;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleTxServiceTests {

    @Test
    void createUsesSafeSelfScopeWithoutAcceptingItFromRoleSave() {
        RoleMapper mapper = mock(RoleMapper.class);
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.insert(any(RoleEntity.class))).thenAnswer(invocation -> {
            RoleEntity entity = invocation.getArgument(0);
            assertEquals("SELF", entity.getDefaultDataScope());
            entity.setId(10L);
            return 1;
        });
        RoleTxService service = service(mapper);
        RoleSaveForm form = form(null, null);

        assertEquals(10L, service.save(form));
    }

    @Test
    void updatePreservesExistingDataScope() {
        RoleMapper mapper = mock(RoleMapper.class);
        RoleEntity entity = new RoleEntity();
        entity.setId(10L);
        entity.setVersion(2);
        entity.setNumber("buyer");
        entity.setDefaultDataScope("ORG");
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.selectById(10L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        service(mapper).save(form(10L, 2));

        assertEquals("ORG", entity.getDefaultDataScope());
        verify(mapper).updateById(entity);
    }

    @Test
    void dataScopeAssignmentRejectsAdminBeforeWrites() {
        RoleMapper mapper = scopeMapper("admin");
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);
        RoleTxService service = new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration);

        BizException error = assertThrows(BizException.class, () -> service.assignDataScopes(scopeForm(List.of())));

        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), error.getCode());
        verify(mapper, never()).updateById(any(RoleEntity.class));
        verifyNoInteractions(configuration);
    }

    @Test
    void dataScopeAssignmentRejectsStaleVersionBeforeWrites() {
        RoleMapper mapper = scopeMapper("buyer");
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);
        RoleDataScopeAssignForm form = scopeForm(List.of());
        form.setVersion(1);

        BizException error = assertThrows(BizException.class, () ->
                new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration).assignDataScopes(form));

        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), error.getCode());
        verify(mapper, never()).updateById(any(RoleEntity.class));
        verifyNoInteractions(configuration);
    }

    @Test
    void dataScopeAssignmentRejectsNullAndEmptyActionDuplicates() {
        RoleMapper mapper = scopeMapper("buyer");
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);
        RoleDataScopeRuleForm first = scopeRule(null, "ORG", List.of());
        RoleDataScopeRuleForm duplicate = scopeRule("", "SELF", List.of());

        BizException error = assertThrows(BizException.class, () ->
                new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration)
                        .assignDataScopes(scopeForm(List.of(first, duplicate))));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), error.getCode());
        verify(mapper, never()).updateById(any(RoleEntity.class));
        verifyNoInteractions(configuration);
    }

    @Test
    void dataScopeAssignmentStopsOnOptimisticUpdateConflict() {
        RoleMapper mapper = scopeMapper("buyer");
        when(mapper.updateById(any(RoleEntity.class))).thenReturn(0);
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);

        BizException error = assertThrows(BizException.class, () ->
                new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration)
                        .assignDataScopes(scopeForm(List.of())));

        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), error.getCode());
        verifyNoInteractions(configuration);
    }

    @Test
    void dataScopeAssignmentRejectsEmptyCustomOrganizations() {
        RoleMapper mapper = scopeMapper("buyer");
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);

        BizException error = assertThrows(BizException.class, () ->
                new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration)
                        .assignDataScopes(scopeForm(List.of(scopeRule(null, "CUSTOM_ORGS", List.of())))));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), error.getCode());
        // 此处是事务内失败契约；真正的数据库回滚由事务集成验证承担。
        verifyNoInteractions(configuration);
    }

    @Test
    void dataScopeAssignmentPreservesResourceActionAndOrganizationPayload() {
        RoleMapper mapper = scopeMapper("buyer");
        DataScopeConfigurationService configuration = mock(DataScopeConfigurationService.class);
        List<Long> organizations = List.of(12L, 11L, 12L);
        RoleDataScopeRuleForm resourceRule = scopeRule(null, "ORG", List.of());
        RoleDataScopeRuleForm actionRule = scopeRule("VIEW", "CUSTOM_ORGS", organizations);

        new RoleTxService(mapper, mock(RolePermissionMapper.class), configuration)
                .assignDataScopes(scopeForm(List.of(resourceRule, actionRule)));

        assertEquals("ORG", mapper.selectById(10L).getDefaultDataScope());
        verify(configuration).replaceRoleRules(10L, List.of(
                new DataScopeRuleSnapshot("purchase", null, "ORG", List.of()),
                new DataScopeRuleSnapshot("purchase", "VIEW", "CUSTOM_ORGS", organizations)));
    }

    private RoleMapper scopeMapper(String roleNumber) {
        RoleMapper mapper = mock(RoleMapper.class);
        RoleEntity role = new RoleEntity();
        role.setId(10L);
        role.setVersion(2);
        role.setNumber(roleNumber);
        role.setDefaultDataScope("SELF");
        when(mapper.selectById(10L)).thenReturn(role);
        when(mapper.updateById(any(RoleEntity.class))).thenReturn(1);
        return mapper;
    }

    private RoleDataScopeAssignForm scopeForm(List<RoleDataScopeRuleForm> rules) {
        RoleDataScopeAssignForm form = new RoleDataScopeAssignForm();
        form.setRoleId(10L);
        form.setVersion(2);
        form.setDefaultDataScope("ORG");
        form.setRules(rules);
        return form;
    }

    private RoleDataScopeRuleForm scopeRule(String action, String scopeType, List<Long> organizations) {
        RoleDataScopeRuleForm rule = new RoleDataScopeRuleForm();
        rule.setResourceType("purchase");
        rule.setAction(action);
        rule.setScopeType(scopeType);
        rule.setOrgIds(organizations);
        return rule;
    }

    private RoleTxService service(RoleMapper mapper) {
        return new RoleTxService(mapper, mock(RolePermissionMapper.class),
                mock(DataScopeConfigurationService.class));
    }

    private RoleSaveForm form(Long id, Integer version) {
        RoleSaveForm form = new RoleSaveForm();
        form.setId(id);
        form.setVersion(version);
        form.setNumber("buyer");
        form.setName("采购员");
        form.setDescription("采购角色");
        return form;
    }
}
