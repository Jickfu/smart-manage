package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.helper.Argon2Helper;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductionAdministratorCredentialInitializerTests {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "production-administrator-test"),
                UserEntity.class);
    }

    @Test
    void rejectsDemoPasswordBeforeReadingDatabase() {
        UserMapper userMapper = mock(UserMapper.class);
        ProductionAdministratorCredentialInitializer initializer = initializer(userMapper, "admin");

        assertThrows(IllegalStateException.class, () -> initializer.run(null));

        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void doesNotOverwritePasswordChangedByAdministrator() {
        UserMapper userMapper = mock(UserMapper.class);
        UserEntity administrator = administrator("changed-password-hash");
        when(userMapper.selectOne(any())).thenReturn(administrator);
        ProductionAdministratorCredentialInitializer initializer =
                initializer(userMapper, "production-initial-password");

        try (MockedStatic<Argon2Helper> argon2 = mockStatic(Argon2Helper.class)) {
            argon2.when(() -> Argon2Helper.verify("changed-password-hash", "admin")).thenReturn(false);

            initializer.run(null);
        }

        verify(userMapper, never()).update(any());
    }

    @Test
    void replacesDemoPasswordOnce() {
        UserMapper userMapper = mock(UserMapper.class);
        UserEntity administrator = administrator("demo-password-hash");
        when(userMapper.selectOne(any())).thenReturn(administrator);
        when(userMapper.update(any())).thenReturn(1);
        ProductionAdministratorCredentialInitializer initializer =
                initializer(userMapper, "production-initial-password");

        try (MockedStatic<Argon2Helper> argon2 = mockStatic(Argon2Helper.class)) {
            argon2.when(() -> Argon2Helper.verify("demo-password-hash", "admin")).thenReturn(true);
            argon2.when(() -> Argon2Helper.encode("production-initial-password"))
                    .thenReturn("production-password-hash");

            initializer.run(null);
        }

        verify(userMapper).update(any());
    }

    private static UserEntity administrator(String passwordHash) {
        UserEntity administrator = new UserEntity();
        administrator.setId(1L);
        administrator.setUsername("administrator");
        administrator.setPassword(passwordHash);
        return administrator;
    }

    private static ProductionAdministratorCredentialInitializer initializer(
            UserMapper userMapper, String initialPassword) {
        ProductionAdministratorCredentialInitializer initializer =
                new ProductionAdministratorCredentialInitializer(userMapper);
        try {
            Field field = ProductionAdministratorCredentialInitializer.class
                    .getDeclaredField("initialPassword");
            field.setAccessible(true);
            field.set(initializer, initialPassword);
            return initializer;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
