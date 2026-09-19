package sm.domain.sys.base.user.service;

import sm.domain.sys.base.user.converter.UserConverter;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.service.OrgReferenceService;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
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
    void deletingUserRefreshesDisplayCacheOnlyAfterDeleteSucceeds() {
        UserMapper mapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setId(10L);
        when(mapper.selectById(10L)).thenReturn(user);
        UserTxService txService = mock(UserTxService.class);
        UserCacheInvalidator userCacheInvalidator = mock(UserCacheInvalidator.class);
        UserService service = service(mapper, txService, userCacheInvalidator);

        service.deleteById(10L);

        verify(txService).deleteById(10L);
        verify(userCacheInvalidator).tryRefreshUsers(List.of(10L));
    }

    @Test
    void failedDeleteDoesNotRefreshDisplayCache() {
        UserTxService txService = mock(UserTxService.class);
        doThrow(new BizException(ResultEnum.DATA_CONFLICT)).when(txService).deleteById(10L);
        UserCacheInvalidator userCacheInvalidator = mock(UserCacheInvalidator.class);
        UserService service = service(mock(UserMapper.class), txService, userCacheInvalidator);

        assertThrows(BizException.class, () -> service.deleteById(10L));

        verify(userCacheInvalidator, never()).tryRefreshUsers(any());
    }

    @Test
    void disablingUsersRefreshesDisplayCacheOnlyAfterUpdateSucceeds() {
        UserTxService txService = mock(UserTxService.class);
        UserCacheInvalidator userCacheInvalidator = mock(UserCacheInvalidator.class);
        UserService service = service(mock(UserMapper.class), txService, userCacheInvalidator);

        service.disable(List.of(10L));

        verify(txService).updateEnabled(List.of(10L), false);
        verify(userCacheInvalidator).tryRefreshUsers(List.of(10L));
    }

    private UserService service(UserMapper mapper, UserTxService txService,
            UserCacheInvalidator userCacheInvalidator) {
        return new UserService(mapper, mock(UserAssignmentMapper.class),
                new OrgReferenceService(mock(OrgMapper.class)), mock(AttachmentService.class), txService,
                userCacheInvalidator, mock(UserConverter.class), mock(CurrentUserContext.class));
    }
}
