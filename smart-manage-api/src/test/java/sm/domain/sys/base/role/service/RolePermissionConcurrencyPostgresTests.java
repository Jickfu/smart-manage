package sm.domain.sys.base.role.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
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
import sm.domain.sys.base.datascope.service.DataScopeConfigurationService;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.form.RolePermissionAssignForm;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** 使用迁移后的真实表、MyBatis Mapper 和 Spring 事务代理验证整体替换，而非等价 SQL 模型。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class RolePermissionConcurrencyPostgresTests {
    private static final long ROLE_ID = -9200001L;
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private RoleTxService service;
    private List<Long> permissions;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var manager = new DataSourceTransactionManager(source);
        transaction = new TransactionTemplate(manager);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(RoleMapper.class);
        configuration.addMapper(RolePermissionMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new ClassPathResource("mapper/sys/base/role/RoleMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        var target = new RoleTxService(session.getMapper(RoleMapper.class),
                session.getMapper(RolePermissionMapper.class), mock(DataScopeConfigurationService.class));
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        service = (RoleTxService) proxy.getProxy();
        jdbc.update("INSERT INTO t_sys_role(id, name, number, version) VALUES (?, '权限并发测试', 'verify-role-lock', 0)", ROLE_ID);
        permissions = jdbc.queryForList("SELECT id FROM t_sys_permission ORDER BY id LIMIT 2", Long.class);
        assertEquals(2, permissions.size());
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM t_sys_role_perms WHERE role_id=?", ROLE_ID);
        jdbc.update("DELETE FROM t_sys_role WHERE id=?", ROLE_ID);
    }

    @Test
    void emptySetReplacementsSerializeInsteadOfFormingUnion() throws Exception {
        assertSerializedReplacement(List.of(permissions.get(1)), false);
    }

    @Test
    void existingSetCanBeReplacedWithEmptySet() throws Exception {
        service.assignPermissions(form(permissions));
        assertSerializedReplacement(List.of(), false);
    }

    @Test
    void concurrentDeleteWaitsForReplacementAndLeavesNoOrphans() throws Exception {
        assertSerializedReplacement(List.of(), true);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_role WHERE id=?", Integer.class, ROLE_ID));
    }

    @Test
    void failedInsertRollsBackDeletionThroughActualTransactionProxy() {
        service.assignPermissions(form(List.of(permissions.get(0))));
        assertThrows(RuntimeException.class, () -> service.assignPermissions(form(List.of(-99999999L))));
        assertEquals(List.of(permissions.get(0)), currentPermissions());
    }

    private void assertSerializedReplacement(List<Long> secondPermissions, boolean deleteRole) throws Exception {
        var firstWritten = new CountDownLatch(1);
        var allowCommit = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        var secondBackend = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                service.assignPermissions(form(List.of(permissions.get(0))));
                firstWritten.countDown();
                await(allowCommit);
            }));
            assertTrue(firstWritten.await(10, TimeUnit.SECONDS));
            var second = executor.submit(() -> transaction.executeWithoutResult(status -> {
                secondBackend.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                secondStarted.countDown();
                if (deleteRole) service.deleteById(ROLE_ID);
                else service.assignPermissions(form(secondPermissions));
            }));
            assertTrue(secondStarted.await(10, TimeUnit.SECONDS));
            // 观察数据库真实锁等待，不能用一次短 sleep 或线程未调度冒充串行化证明。
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            boolean blocked = false;
            while (System.nanoTime() < deadline && !second.isDone()) {
                blocked = Boolean.TRUE.equals(jdbc.queryForObject(
                        "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, secondBackend.get()));
                if (blocked) break;
                Thread.sleep(10);
            }
            assertTrue(blocked, "第二个整体替换必须等待第一个父角色事务");
            allowCommit.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            assertEquals(secondPermissions, currentPermissions());
        } finally {
            allowCommit.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private List<Long> currentPermissions() {
        return jdbc.queryForList("SELECT permission_id FROM t_sys_role_perms WHERE role_id=? ORDER BY permission_id", Long.class, ROLE_ID);
    }

    private RolePermissionAssignForm form(List<Long> values) {
        var form = new RolePermissionAssignForm();
        form.setRoleId(ROLE_ID);
        form.setPermissionIds(values);
        return form;
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("并发测试等待超时");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发测试被中断", exception);
        }
    }
}
