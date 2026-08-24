package sm.domain.sys.base.role.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.system.resource.BusinessResourceRegistry;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleSelectForm;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
                mock(AuthorizationStateHelper.class),
                new RoleConverterImpl(), mock(RoleDataScopeMapper.class), mock(RoleDataScopeOrgMapper.class),
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
                mock(AuthorizationStateHelper.class),
                new RoleConverterImpl(), mock(RoleDataScopeMapper.class), mock(RoleDataScopeOrgMapper.class),
                mock(BusinessResourceRegistry.class));

        assertEquals(List.of("operator", "reviewer"), service.getUserRoleNumbers(10L, 20L));
    }
}
