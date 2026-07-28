package sm.domain.sys.base.role.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleServiceTests {

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
