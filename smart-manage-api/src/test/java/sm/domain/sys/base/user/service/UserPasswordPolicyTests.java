package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.model.UserCredentialSnapshot;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import sm.system.helper.Argon2Helper;
import sm.system.security.context.CurrentUserContext;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 验证真实写入入口均执行策略，不能绕过页面校验直接写入弱密码。 */
class UserPasswordPolicyTests {
    private final UserMapper mapper = mock(UserMapper.class);
    private final WeakPasswordMapper blacklist = mock(WeakPasswordMapper.class);
    private final PasswordPolicyService policy = new PasswordPolicyService(blacklist);
    private final UserWriter writer = new UserWriter(mapper, mock(UserRoleMapper.class),
            mock(UserAssignmentMapper.class), mock(OrgReferenceReader.class), policy);
    private final UserTxService service = new UserTxService(mapper, mock(UserRoleMapper.class),
            mock(UserAssignmentMapper.class), mock(OrgReferenceReader.class), mock(CurrentUserContext.class), writer, policy);
    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(42L);
        user.setUsername("policy-test-user");
        user.setPassword(Argon2Helper.encode("original long passphrase"));
        user.setPasswordReset(true);
        user.setEnabled(true);
        user.setCredentialGeneration(0L);
        user.setEmailVerifiedAt(LocalDateTime.now());
        when(mapper.selectById(42L)).thenReturn(user);
        when(blacklist.containsDigest(WeakPasswordUtil.digest("blocked long passphrase"))).thenReturn(true);
    }

    @Test
    void allUserChosenPasswordPathsRejectShortAndBlacklistedValuesWithoutWrites() {
        for (String password : new String[]{"short", "blocked long passphrase"}) {
            assertThrows(BizException.class, () -> service.changeResetPassword(42L, 0L, password));
            assertThrows(BizException.class, () -> service.updateCurrentPassword(42L, "original long passphrase", password));
            assertThrows(BizException.class, () -> service.updatePasswordByVerifiedEmail(
                    new UserCredentialSnapshot(42L, "policy@example.invalid", 0L), password));
            UserSaveForm form = new UserSaveForm();
            form.setUsername("another-user");
            form.setNumber("another-user");
            form.setName("测试用户");
            form.setPassword(password);
            assertThrows(BizException.class, () -> writer.save(form, 43L));
        }
        verify(mapper, never()).insert(any(UserEntity.class));
        verify(mapper, never()).updateById(any(UserEntity.class));
        verify(mapper, never()).changeResetPassword(any(), any(), any());
        verify(mapper, never()).updatePasswordByVerifiedEmail(any(), any());
    }

    @Test
    void administratorGeneratedResetPasswordIsTwentyCharactersAndChecksBlacklist() {
        user.setUsername("administrator");
        when(mapper.updateById(any(UserEntity.class))).thenReturn(1);
        String password = service.resetAdministratorPassword(42L);
        assertEquals(20, password.length());
        verify(blacklist).containsDigest(WeakPasswordUtil.digest(password));
        assertTrue(Argon2Helper.verify(user.getPassword(), password));
        assertTrue(user.getPasswordReset());
    }

    @Test
    void acceptsFullUnicodePasswordWithoutTruncatingItsStoredValue() {
        when(mapper.updateById(any(UserEntity.class))).thenReturn(1);
        String password = "😀".repeat(64);
        service.updateCurrentPassword(42L, "original long passphrase", password);
        assertTrue(Argon2Helper.verify(user.getPassword(), password));
        assertFalse(Argon2Helper.verify(user.getPassword(), "😀".repeat(63)));
        assertFalse(user.getPasswordReset());
    }

    @Test
    void databaseFailurePreventsCredentialWrite() {
        when(blacklist.containsDigest(anyString())).thenThrow(new IllegalStateException("数据库不可用"));
        assertThrows(IllegalStateException.class, () -> service.changeResetPassword(42L, 0L, "quiet river under moonlight"));
        verify(mapper, never()).changeResetPassword(any(), any(), any());
    }
}
