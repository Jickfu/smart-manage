package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.system.helper.Argon2Helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserAuthenticationServiceTests {
    @Test
    void authenticatesEnabledUserWithAvailablePrimaryOrganization() {
        UserMapper mapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("user");
        user.setPassword("encoded");
        user.setEnabled(true);
        when(mapper.selectOne(any())).thenReturn(user);
        UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
        UserAssignmentEntity assignment = new UserAssignmentEntity();
        assignment.setOrgId(20L);
        when(assignmentMapper.selectOne(any())).thenReturn(assignment);
        OrgMapper orgMapper = mock(OrgMapper.class);
        OrgEntity organization = new OrgEntity();
        organization.setId(20L);
        organization.setEnabled(true);
        organization.setArchived(false);
        when(orgMapper.selectById(20L)).thenReturn(organization);
        UserAuthenticationService service = new UserAuthenticationService(mapper, assignmentMapper,
                orgMapper, mock(UserTxService.class), mock(AuthorizationStateHelper.class));

        try (MockedStatic<Argon2Helper> helper = mockStatic(Argon2Helper.class)) {
            helper.when(() -> Argon2Helper.verify("encoded", "password")).thenReturn(true);
            UserAuthentication authentication = service.authenticate("user", "password");
            assertTrue(authentication.successful());
            assertFalse(authentication.administrator());
        }
    }

    @Test
    void temporaryLoginDoesNotAllowAdministrator() {
        UserMapper mapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("administrator");
        user.setEnabled(true);
        when(mapper.selectById(1L)).thenReturn(user);
        UserAuthenticationService service = new UserAuthenticationService(mapper,
                mock(UserAssignmentMapper.class), mock(OrgMapper.class),
                mock(UserTxService.class), mock(AuthorizationStateHelper.class));

        assertFalse(service.authenticateTemporaryLogin(1L, "administrator").successful());
    }
}
