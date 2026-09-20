package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.helper.Argon2Helper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class AdministratorCredentialInitializerTests {
    private final UserMapper mapper = mock(UserMapper.class);
    private final PasswordPolicyService policy = mock(PasswordPolicyService.class);
    private final AdministratorInitialPasswordFile passwordFile = mock(AdministratorInitialPasswordFile.class);

    @BeforeAll
    static void initializeEntityMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(
                new MybatisConfiguration(), "administrator-initializer-test"), UserEntity.class);
    }

    @Test
    void rejectedInitialPasswordDoesNotWriteCredentials() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        doThrow(new IllegalArgumentException("不符合密码策略")).when(policy).validate("weak", "administrator");
        when(passwordFile.prepare()).thenReturn(prepared("weak"));
        assertThrows(IllegalArgumentException.class, () -> initializer().run(null));
        verify(mapper, never()).update(any());
    }

    @Test
    void initializedAdministratorDoesNotRequireConfigurationOrOverwriteCredentials() {
        when(mapper.selectOne(any())).thenReturn(administrator("existing-hash"));
        initializer().run(null);
        verify(mapper, never()).update(any());
        verifyNoInteractions(policy);
        verifyNoInteractions(passwordFile);
    }

    @Test
    void initialPasswordFileFailurePreventsFirstStartup() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        when(passwordFile.prepare()).thenThrow(new IllegalStateException("无法准备管理员初始密码文件"));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> initializer().run(null));
        assertTrue(exception.getMessage().contains("无法准备管理员初始密码文件"));
        verify(mapper, never()).update(any());
    }

    @Test
    void initialPasswordIsValidatedAndWrittenOnce() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        when(mapper.update(any())).thenReturn(1);
        when(passwordFile.prepare()).thenReturn(prepared("test-initial-password"));
        try (var hashing = mockStatic(Argon2Helper.class)) {
            hashing.when(() -> Argon2Helper.encode("test-initial-password")).thenReturn("encoded");
            initializer().run(null);
        }
        verify(policy).validate("test-initial-password", "administrator");
        verify(mapper).update(any());
    }

    @Test
    void concurrentWinnerIsPreservedAndMissingWinnerFails() {
        when(mapper.selectOne(any())).thenReturn(administrator(""));
        when(mapper.update(any())).thenReturn(0);
        when(passwordFile.prepare()).thenReturn(prepared("test-initial-password"));
        try (var hashing = mockStatic(Argon2Helper.class)) {
            hashing.when(() -> Argon2Helper.encode("test-initial-password")).thenReturn("encoded");
            hashing.when(() -> Argon2Helper.verify("winner", "test-initial-password")).thenReturn(true);
            when(mapper.selectById(1L)).thenReturn(administrator("winner"));
            initializer().run(null);
            when(mapper.selectById(1L)).thenReturn(administrator(""));
            assertThrows(IllegalStateException.class, () -> initializer().run(null));
        }
    }

    private AdministratorCredentialInitializer initializer() {
        return new AdministratorCredentialInitializer(mapper, policy, passwordFile);
    }

    private AdministratorInitialPasswordFile.PreparedPassword prepared(String password) {
        return new AdministratorInitialPasswordFile.PreparedPassword(password,
                Path.of(AdministratorInitialPasswordFile.FILE_NAME).toAbsolutePath());
    }

    private static UserEntity administrator(String hash) {
        var administrator = new UserEntity();
        administrator.setId(1L);
        administrator.setPassword(hash);
        return administrator;
    }
}
