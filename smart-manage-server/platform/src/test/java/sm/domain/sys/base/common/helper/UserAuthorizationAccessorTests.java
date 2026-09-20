package sm.domain.sys.base.common.helper;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthorizationAccessorTests {
    @Test
    void eachDecisionReadsCurrentDatabaseGrantsWithoutCacheFallback() {
        var permissions = mock(PermissionMapper.class);
        var roles = mock(RoleMapper.class);
        when(permissions.selectUserPermissionNumbers(1L, 2L, null))
                .thenReturn(List.of("before"), List.of());
        when(roles.selectUserRoleNumbers(1L, 2L)).thenReturn(List.of("role"));
        var accessor = new UserAuthorizationAccessor(permissions, roles);
        assertEquals(List.of("before"), accessor.get(1L, 2L).getPermissionNumbers());
        assertEquals(List.of(), accessor.get(1L, 2L).getPermissionNumbers());
        when(permissions.selectUserPermissionNumbers(1L, 2L, null)).thenThrow(new IllegalStateException());
        assertThrows(IllegalStateException.class, () -> accessor.get(1L, 2L));
    }
}
