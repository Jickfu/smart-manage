package sm.domain.sys.base.role.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.entity.RoleEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleServiceTests {

    @Test
    void listAllIncludesDescriptionForRoleAssignment() {
        RoleMapper mapper = mock(RoleMapper.class);
        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setNumber("reviewer");
        role.setName("审核角色");
        role.setDescription("负责审核业务单据");
        when(mapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(role));
        RoleService service = new RoleService(
                mapper,
                mock(RolePermissionMapper.class),
                mock(RoleTxService.class),
                mock(AuthorizationStateHelper.class),
                new RoleConverterImpl());

        var result = service.listAll();

        assertEquals(1, result.size());
        assertEquals("负责审核业务单据", result.getFirst().getDescription());
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
                new RoleConverterImpl());

        assertEquals(List.of("operator", "reviewer"), service.getUserRoleNumbers(10L, 20L));
    }
}
