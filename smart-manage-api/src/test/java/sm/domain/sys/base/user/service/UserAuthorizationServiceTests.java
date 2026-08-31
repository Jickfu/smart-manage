package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.service.OrgReferenceService;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.form.UserOrganizationRoleForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.system.security.context.CurrentUserContext;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAuthorizationServiceTests {
    @Test
    void roleAssignmentRefreshesPreviousAndSubmittedOrganizations() {
        UserRoleMapper roleMapper = mock(UserRoleMapper.class);
        when(roleMapper.selectOrgIdsByUserId(10L)).thenReturn(List.of(20L));
        UserTxService txService = mock(UserTxService.class);
        AuthorizationStateHelper stateHelper = mock(AuthorizationStateHelper.class);
        UserAuthorizationService service = new UserAuthorizationService(mock(UserMapper.class), roleMapper,
                mock(UserAssignmentMapper.class), new OrgReferenceService(mock(OrgMapper.class)), txService,
                mock(PermissionService.class), stateHelper, mock(CurrentUserContext.class));
        UserOrganizationRoleForm organization = new UserOrganizationRoleForm();
        organization.setOrgId(21L);
        organization.setRoleIds(List.of(30L));
        UserRoleAssignmentSaveForm form = new UserRoleAssignmentSaveForm();
        form.setUserId(10L);
        form.setAssignments(List.of(organization));

        service.saveRoleAssignment(form);

        verify(txService).saveRoleAssignment(form);
        verify(stateHelper).refreshUserAuthorization(10L, 20L);
        verify(stateHelper).refreshUserAuthorization(10L, 21L);
    }
}
