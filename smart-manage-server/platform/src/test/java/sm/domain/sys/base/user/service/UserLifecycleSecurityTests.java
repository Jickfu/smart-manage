package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.converter.UserConverter;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserLifecycleSecurityTests {
    @Test
    void publicCommandsRejectReservedNameAndDatabaseTargetBeforeSideEffects() {
        var mapper = mock(UserMapper.class);
        var administrator = administrator();
        when(mapper.selectById(1L)).thenReturn(administrator);
        when(mapper.selectByIds(List.of(10L, 1L))).thenReturn(List.of(administrator));
        var transaction = mock(UserTxService.class);
        var attachment = mock(AttachmentService.class);
        var cache = mock(UserCacheInvalidator.class);
        var service = new UserService(mapper, mock(UserAssignmentMapper.class),
                mock(OrgReferenceReader.class), attachment, transaction, cache,
                mock(UserConverter.class), mock(CurrentUserContext.class));

        assertDenied(() -> service.save(form(null, "administrator")));
        assertDenied(() -> service.save(form(1L, "administrator")));
        assertDenied(() -> service.save(form(1L, "forged-ordinary-name")));
        assertDenied(() -> service.deleteById(1L));
        assertDenied(() -> service.enable(List.of(10L, 1L)));
        assertDenied(() -> service.disable(List.of(10L, 1L)));
        verifyNoInteractions(transaction, attachment, cache);
    }

    @Test
    void transactionCommandsAndSharedWriterCannotBypassTargetProtection() {
        var mapper = mock(UserMapper.class);
        var roles = mock(UserRoleMapper.class);
        var assignments = mock(UserAssignmentMapper.class);
        var organizations = mock(OrgReferenceReader.class);
        var policy = mock(PasswordPolicyService.class);
        var attachment = mock(AttachmentGateway.class);
        var writer = new UserWriter(mapper, roles, assignments, organizations, policy);
        when(mapper.selectById(1L)).thenReturn(administrator());
        when(mapper.selectByIds(List.of(10L, 1L))).thenReturn(List.of(administrator()));
        var transaction = new UserTxService(mapper, roles, assignments, organizations,
                mock(CurrentUserContext.class), writer, policy, attachment);

        assertDenied(() -> transaction.save(form(null, "administrator")));
        assertDenied(() -> transaction.save(form(1L, "forged-ordinary-name"), 1L));
        assertDenied(() -> transaction.deleteById(1L));
        assertDenied(() -> transaction.updateEnabled(List.of(10L, 1L), false));
        assertDenied(() -> transaction.updateEnabled(List.of(10L, 1L), true));
        assertDenied(() -> writer.save(form(null, "administrator"), 99L));
        assertDenied(() -> writer.save(form(1L, "forged-ordinary-name"), 1L));
        verifyNoInteractions(roles, assignments, organizations, policy, attachment);
        verify(mapper, never()).insert(any(UserEntity.class));
        verify(mapper, never()).updateById(any(UserEntity.class));
        verify(mapper, never()).deleteById(anyLong());
    }

    private UserEntity administrator() {
        var target = new UserEntity();
        target.setId(1L);
        target.setUsername("administrator");
        return target;
    }

    private UserSaveForm form(Long id, String username) {
        var form = new UserSaveForm();
        form.setId(id);
        form.setUsername(username);
        form.setAvatarAttachmentId(20L);
        form.setAttachmentUploadSessions(Map.of(20L, "test-upload-session"));
        return form;
    }

    private void assertDenied(Runnable command) {
        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), assertThrows(BizException.class, command::run).getCode());
    }
}
