package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.model.UserCredentialSnapshot;
import sm.system.exception.BizException;
import sm.system.helper.Argon2Helper;
import sm.system.security.context.CurrentUserContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** 用真实 Flyway 触发器、MyBatis CAS 和 Spring 事务验证安全代际，不用 mock 代替数据库竞态。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class UserCredentialGenerationPostgresTests {
    private static final long USER_ID = 9200000101L;
    private JdbcTemplate jdbc;
    private UserMapper mapper;
    private UserTxService service;
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var manager = new DataSourceTransactionManager(source);
        transaction = new TransactionTemplate(manager);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(UserMapper.class);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(interceptor);
        factory.setMapperLocations(new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"),
                new ClassPathResource("mapper/sys/base/user/UserMapper.xml"));
        mapper = new SqlSessionTemplate(factory.getObject()).getMapper(UserMapper.class);
        var target = new UserTxService(mapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
                mock(OrgReferenceReader.class), mock(CurrentUserContext.class), mock(UserWriter.class));
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        service = (UserTxService) proxy.getProxy();
        jdbc.update("""
                INSERT INTO t_sys_user(id, username, name, number, password, email, email_verified_at,
                    password_reset, enabled) VALUES (?, 'verify-credential-user', '凭据测试',
                    'verify-credential-user', ?, 'old@example.invalid', now(), false, true)
                """, USER_ID, Argon2Helper.encode("initial-test-password"));
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM t_sys_user WHERE id=?", USER_ID);
    }

    @Test
    void ordinaryUpdatesPreserveGenerationAndSecurityUpdateAdvancesExactlyOnce() {
        var user = mapper.selectById(USER_ID);
        user.setName("改名不撤销凭据");
        user.setCredentialGeneration(999L);
        assertEquals(1, mapper.updateById(user));
        assertEquals(0L, mapper.selectById(USER_ID).getCredentialGeneration());
        assertEquals(1, mapper.selectById(USER_ID).getVersion());
        user = mapper.selectById(USER_ID);
        user.setEmail("new@example.invalid");
        assertEquals(1, mapper.updateById(user));
        assertEquals(1L, mapper.selectById(USER_ID).getCredentialGeneration());
        assertEquals(2, user.getVersion());
        assertEquals(user.getVersion(), mapper.selectById(USER_ID).getVersion());
    }

    @Test
    void disableReenableAndRepeatedSameStateNeverRestoreOldProof() {
        var original = snapshot();
        var staleEntity = mapper.selectById(USER_ID);
        jdbc.update("UPDATE t_sys_user SET enabled=false WHERE id=?", USER_ID);
        jdbc.update("UPDATE t_sys_user SET enabled=false WHERE id=?", USER_ID);
        assertEquals(1L, snapshot().generation());
        jdbc.update("UPDATE t_sys_user SET enabled=true WHERE id=?", USER_ID);
        assertEquals(2L, snapshot().generation());
        assertEquals(0, mapper.updatePasswordByVerifiedEmail(original, "must-not-write"));
        staleEntity.setName("过期编辑");
        assertEquals(0, mapper.updateById(staleEntity));
        assertThrows(BizException.class, () -> new UserSessionStateVerifier(mapper).verify(USER_ID, 0));
        assertDoesNotThrow(() -> new UserSessionStateVerifier(mapper).verify(USER_ID, 2));
    }

    @Test
    void bindingAndRestoringSameMailboxDoesNotRestoreOldGeneration() {
        var original = snapshot();
        service.bindVerifiedEmail(original, "new@example.invalid");
        service.bindVerifiedEmail(snapshot(), "old@example.invalid");
        assertEquals(2L, snapshot().generation());
        assertThrows(BizException.class, () -> service.updatePasswordByVerifiedEmail(original, "new-password"));
        assertEquals(2L, snapshot().generation());
    }

    @Test
    void successfulCasRevokesOtherProofAndRollbackRestoresGeneration() {
        var original = snapshot();
        transaction.executeWithoutResult(status -> {
            service.updatePasswordByVerifiedEmail(original, "changed-password");
            assertEquals(1L, snapshot().generation());
            status.setRollbackOnly();
        });
        assertEquals(0L, snapshot().generation());
        service.updatePasswordByVerifiedEmail(original, "changed-password");
        assertEquals(1L, snapshot().generation());
        assertEquals(1, mapper.selectById(USER_ID).getVersion());
        assertTrue(Argon2Helper.verify(mapper.selectById(USER_ID).getPassword(), "changed-password"));
        assertEquals(0, mapper.updatePasswordByVerifiedEmail(original, "must-not-write"));
    }

    @Test
    void secondAdministratorResetInvalidatesFirstPasswordChangeTicket() {
        service.resetPassword(USER_ID);
        long firstGeneration = snapshot().generation();
        service.resetPassword(USER_ID);
        assertThrows(BizException.class, () -> service.changeResetPassword(USER_ID, firstGeneration, "new-password"));
        service.changeResetPassword(USER_ID, snapshot().generation(), "new-password");
        assertFalse(mapper.selectById(USER_ID).getPasswordReset());
    }

    @Test
    void consumeThenConcurrentBindingCannotPassFinalPasswordCas() throws Exception {
        var consumedSnapshot = snapshot();
        var bindingWritten = new CountDownLatch(1);
        var releaseBinding = new CountDownLatch(1);
        var passwordStarted = new CountDownLatch(1);
        var passwordBackend = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(2);
        try {
            var binding = executor.submit(() -> transaction.executeWithoutResult(status -> {
                service.bindVerifiedEmail(consumedSnapshot, "new@example.invalid");
                bindingWritten.countDown();
                await(releaseBinding);
            }));
            assertTrue(bindingWritten.await(10, TimeUnit.SECONDS));
            var password = executor.submit(() -> assertThrows(BizException.class,
                    () -> transaction.executeWithoutResult(status -> {
                        passwordBackend.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                        passwordStarted.countDown();
                        service.updatePasswordByVerifiedEmail(consumedSnapshot, "must-not-write");
                    })));
            assertTrue(passwordStarted.await(10, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            boolean blocked = false;
            while (System.nanoTime() < deadline && !password.isDone()) {
                blocked = Boolean.TRUE.equals(jdbc.queryForObject(
                        "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, passwordBackend.get()));
                if (blocked) break;
                Thread.sleep(10);
            }
            assertTrue(blocked, "密码最终 CAS 必须实际等待并发邮箱事务");
            releaseBinding.countDown();
            binding.get(10, TimeUnit.SECONDS);
            password.get(10, TimeUnit.SECONDS);
            assertEquals("new@example.invalid", snapshot().email());
            assertEquals(1L, snapshot().generation());
            assertTrue(Argon2Helper.verify(mapper.selectById(USER_ID).getPassword(), "initial-test-password"));
        } finally {
            releaseBinding.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private UserCredentialSnapshot snapshot() {
        var user = mapper.selectById(USER_ID);
        return new UserCredentialSnapshot(user.getId(), user.getEmail(), user.getCredentialGeneration());
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("凭据并发测试超时");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("凭据并发测试被中断", exception);
        }
    }
}
