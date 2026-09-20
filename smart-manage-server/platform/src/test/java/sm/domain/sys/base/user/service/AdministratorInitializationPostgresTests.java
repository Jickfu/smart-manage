package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.helper.Argon2Helper;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 只在脚本创建的临时数据库验证真实 Mapper 条件更新与触发器的并发语义。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class AdministratorInitializationPostgresTests {
    @Test
    void concurrentInitializationWritesExactlyOnceAndRestartNeverOverwrites() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var jdbc = new JdbcTemplate(source);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(UserMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        var mapper = new SqlSessionTemplate(factory.getObject()).getMapper(UserMapper.class);
        var before = mapper.selectById(1L);
        assertEquals("", before.getPassword());
        var barrier = new CyclicBarrier(2);
        var policy = mock(PasswordPolicyService.class);
        // 两个实例都已读到未初始化标记，再同时竞争同一条数据库记录。
        doAnswer(invocation -> { barrier.await(10, TimeUnit.SECONDS); return null; })
                .when(policy).validate(anyString(), eq("administrator"));
        String firstPassword = "Fixture-First-724!";
        String secondPassword = "Fixture-Second-829!";
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> initializer(mapper, policy, firstPassword).run(null));
            var second = executor.submit(() -> initializer(mapper, policy, secondPassword).run(null));
            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
            var after = mapper.selectById(1L);
            assertEquals(before.getCredentialGeneration() + 1, after.getCredentialGeneration());
            assertTrue(after.getPasswordReset());
            assertTrue(Argon2Helper.verify(after.getPassword(), firstPassword)
                    ^ Argon2Helper.verify(after.getPassword(), secondPassword));
            initializer(mapper, policy, null).run(null);
            initializer(mapper, policy, "ignored-new-value").run(null);
            assertEquals(after.getPassword(), mapper.selectById(1L).getPassword());
            verify(policy, times(2)).validate(anyString(), eq("administrator"));
        } finally {
            jdbc.update("UPDATE t_sys_user SET password='', password_reset=true WHERE id=1");
        }
    }

    private AdministratorCredentialInitializer initializer(UserMapper mapper, PasswordPolicyService policy, String password) {
        AdministratorInitialPasswordFile passwordFile = mock(AdministratorInitialPasswordFile.class);
        when(passwordFile.prepare()).thenReturn(new AdministratorInitialPasswordFile.PreparedPassword(
                password, Path.of(AdministratorInitialPasswordFile.FILE_NAME).toAbsolutePath()));
        return new AdministratorCredentialInitializer(mapper, policy, passwordFile);
    }
}
