package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.helper.Argon2Helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class AdministratorCredentialInitializerTests {
    private final UserMapper mapper = mock(UserMapper.class);
    private final PasswordPolicyService policy = mock(PasswordPolicyService.class);

    @Test
    void rejectedInitialPasswordDoesNotWriteCredentials() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        doThrow(new IllegalArgumentException("不符合密码策略")).when(policy).validate("weak", "administrator");
        assertThrows(IllegalArgumentException.class, () -> initializer("weak").run(null));
        verify(mapper, never()).update(any());
    }

    @Test
    void initializedAdministratorDoesNotRequireConfigurationOrOverwriteCredentials() {
        when(mapper.selectOne(any())).thenReturn(administrator("existing-hash"));
        initializer(null).run(null);
        verify(mapper, never()).update(any());
        verifyNoInteractions(policy);
    }

    @Test
    void missingInitialPasswordPreventsFirstStartup() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        assertThrows(IllegalStateException.class, () -> initializer(null).run(null));
        verify(mapper, never()).update(any());
    }

    @Test
    void initialPasswordIsValidatedAndWrittenOnce() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        when(mapper.update(any())).thenReturn(1);
        try (var hashing = mockStatic(Argon2Helper.class)) {
            hashing.when(() -> Argon2Helper.encode("test-initial-password")).thenReturn("encoded");
            initializer("test-initial-password").run(null);
        }
        verify(policy).validate("test-initial-password", "administrator");
        verify(mapper).update(any());
    }

    @Test
    void concurrentWinnerIsPreservedAndMissingWinnerFails() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        when(mapper.update(any())).thenReturn(0);
        try (var hashing = mockStatic(Argon2Helper.class)) {
            hashing.when(() -> Argon2Helper.encode("test-initial-password")).thenReturn("encoded");
            when(mapper.selectById(1L)).thenReturn(administrator("winner"));
            initializer("test-initial-password").run(null);
            when(mapper.selectById(1L)).thenReturn(administrator(""));
            assertThrows(IllegalStateException.class, () -> initializer("test-initial-password").run(null));
        }
    }

    private AdministratorCredentialInitializer initializer(String password) {
        var initializer = new AdministratorCredentialInitializer(mapper, policy);
        ReflectionTestUtils.setField(initializer, "initialPassword", password);
        return initializer;
    }

    private static UserEntity administrator(String hash) {
        var administrator = new UserEntity();
        administrator.setId(1L);
        administrator.setPassword(hash);
        return administrator;
    }
}
