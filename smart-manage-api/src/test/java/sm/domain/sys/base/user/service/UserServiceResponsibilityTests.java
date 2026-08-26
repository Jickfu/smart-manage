package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.auth.SessionTerminationReason;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceResponsibilityTests {
    @Test
    void deletingUserTerminatesSessionsOnlyAfterDeleteSucceeds() {
        UserMapper mapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setId(10L);
        when(mapper.selectById(10L)).thenReturn(user);
        UserTxService txService = mock(UserTxService.class);
        AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
        UserService service = service(mapper, txService, authorizationStateHelper);

        service.deleteById(10L);

        verify(txService).deleteById(10L);
        verify(authorizationStateHelper).terminateUsers(
                List.of(10L), SessionTerminationReason.ACCOUNT_DELETED);
    }

    @Test
    void failedDeleteDoesNotTerminateSessions() {
        UserTxService txService = mock(UserTxService.class);
        doThrow(new BizException(ResultEnum.DATA_CONFLICT)).when(txService).deleteById(10L);
        AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
        UserService service = service(mock(UserMapper.class), txService, authorizationStateHelper);

        assertThrows(BizException.class, () -> service.deleteById(10L));

        verify(authorizationStateHelper, never()).terminateUsers(any(), any());
    }

    @Test
    void disablingUsersTerminatesSessionsOnlyAfterUpdateSucceeds() {
        UserTxService txService = mock(UserTxService.class);
        AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
        UserService service = service(mock(UserMapper.class), txService, authorizationStateHelper);

        service.disable(List.of(10L));

        verify(txService).updateEnabled(List.of(10L), false);
        verify(authorizationStateHelper).terminateUsers(
                List.of(10L), SessionTerminationReason.ACCOUNT_DISABLED);
    }

    private UserService service(UserMapper mapper, UserTxService txService,
            AuthorizationStateHelper authorizationStateHelper) {
        return new UserService(mapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
                mock(OrgMapper.class), mock(AttachmentService.class), txService,
                authorizationStateHelper, mock(UserConverter.class), mock(CurrentUserContext.class));
    }
}
