package sm.domain.sys.base.role.service;

import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.role.model.form.RoleDefaultSaveForm;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.system.exception.BizException;
import java.util.concurrent.ExecutionException;
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
import sm.domain.sys.base.role.mapper.RoleDefaultMapper;
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
    private RoleMapper roleMapper;
    private UserMapper userReader;
    private RoleDefaultMapper defaultReader;
    private AppMapper appReader;
    private MenuMapper menuReader;
    private PermissionMapper permissionReader;
    private RoleDataScopeMapper scopeReader;
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
        configuration.addMapper(UserMapper.class);
        configuration.addMapper(AppMapper.class);
        configuration.addMapper(MenuMapper.class);
        configuration.addMapper(PermissionMapper.class);
        configuration.addMapper(RoleDataScopeMapper.class);
        configuration.addMapper(RoleDefaultMapper.class);
        configuration.addMapper(RolePermissionMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new ClassPathResource("mapper/sys/base/role/RoleMapper.xml"),
                new ClassPathResource("mapper/sys/base/role/RoleDefaultMapper.xml"),
                new ClassPathResource("mapper/sys/base/user/UserMapper.xml"),
                new ClassPathResource("mapper/sys/base/permission/PermissionMapper.xml"),
                new ClassPathResource("mapper/sys/base/datascope/RoleDataScopeMapper.xml"),
                new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"),
                new ClassPathResource("mapper/sys/base/app/AppMapper.xml"),
                new ClassPathResource("mapper/sys/base/menu/MenuMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        roleMapper = session.getMapper(RoleMapper.class);
        userReader = session.getMapper(UserMapper.class);
        defaultReader = session.getMapper(RoleDefaultMapper.class);
        appReader = session.getMapper(AppMapper.class);
        menuReader = session.getMapper(MenuMapper.class);
        permissionReader = session.getMapper(PermissionMapper.class);
        scopeReader = session.getMapper(RoleDataScopeMapper.class);
        var target = new RoleTxService(session.getMapper(RoleMapper.class), session.getMapper(RoleDefaultMapper.class),
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
        jdbc.update("UPDATE t_sys_role_default SET role_id=NULL WHERE id=1");
        jdbc.update("DELETE FROM t_sys_user_role WHERE user_id=-9200002");
        jdbc.update("DELETE FROM t_sys_user_assignment WHERE user_id=-9200002");
        jdbc.update("DELETE FROM t_sys_user WHERE id=-9200002");
        jdbc.update("DELETE FROM t_sys_org WHERE id=-9200004");
        jdbc.update("DELETE FROM t_sys_role_perms WHERE role_id IN (?, -9200003)", ROLE_ID);
        jdbc.update("DELETE FROM t_sys_role WHERE id=-9200003");
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


    @Test
    void defaultRoleAppliesDynamicallyAndPreservesManualAssignments() {
        jdbc.update("INSERT INTO t_sys_role(id,name,number,version,default_data_scope) VALUES (-9200003,'新默认','verify-new-default',0,'ORG')");
        jdbc.update("INSERT INTO t_sys_user(id,username,name,number,enabled,password) VALUES (-9200002,'verify-default-user','默认测试','verify-default-user',true,'')");
        jdbc.update("INSERT INTO t_sys_org(id,name,number,parent_id,number_path,name_path,org_type) VALUES (-9200004,'默认测试组织','verify-default-org',1,'SM/verify-default-org','SM/默认测试组织','DEPARTMENT')");
        jdbc.update("INSERT INTO t_sys_user_assignment(id,user_id,org_id,is_primary,position) VALUES (-9200005,-9200002,1,true,'测试'),(-9200006,-9200002,-9200004,false,'测试')");
        assertEquals(List.of(), roleMapper.selectUserRoleNumbers(-9200002L,1L));
        // 使用真实菜单入口权限，同时验证应用、菜单与接口权限使用相同有效角色。
        service.assignPermissions(form(List.of(10015L)));
        setDefault(ROLE_ID);
        assertEquals(ROLE_ID, defaultReader.selectConfiguration().getRole().getId());
        transaction.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO t_sys_user(id,username,name,number,enabled,password) VALUES (-9200008,'verify-new-user','新用户','verify-new-user',true,'')");
            jdbc.update("INSERT INTO t_sys_user_assignment(id,user_id,org_id,is_primary,position) VALUES (-9200009,-9200008,1,true,'测试')");
            assertEquals(List.of("verify-role-lock"),roleMapper.selectUserRoleNumbers(-9200008L,1L));
            assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM t_sys_user_role WHERE user_id=-9200008",Integer.class));
            status.setRollbackOnly();
        });
        assertEquals(List.of("verify-role-lock"),roleMapper.selectUserRoleNumbers(-9200002L,1L));
        assertEquals(List.of("verify-role-lock"),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        assertTrue(userReader.selectEnabledExplicitRoleMembers(1L, ROLE_ID).isEmpty());
        assertEquals(List.of(),roleMapper.selectUserRoleNumbers(-9200002L,-999L));
        assertTrue(permissionReader.selectUserPermissionNumbers(-9200002L,1L,null).contains("sys:base:role:listPage"));
        assertFalse(appReader.selectUserDomainApps(-9200002L,1L).isEmpty());
        assertFalse(menuReader.selectUserMenus(-9200002L,1L,31L,false).isEmpty());
        assertTrue(appReader.selectUserDomainApps(-9200002L,-999L).isEmpty());
        assertTrue(menuReader.selectUserMenus(-9200002L,-999L,31L,false).isEmpty());
        assertEquals(1,scopeReader.selectEffectiveRules(-9200002L,1L,"verify.default","VIEW").size());
        assertThrows(BizException.class, () -> service.deleteById(ROLE_ID));
        assertThrows(BizException.class, () -> service.deleteByIds(List.of(-9200003L, ROLE_ID)));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM t_sys_role WHERE id=-9200003",Integer.class));
        jdbc.update("INSERT INTO t_sys_user_role(id,user_id,org_id,role_id) VALUES (-9200007,-9200002,1,?)",ROLE_ID);
        assertEquals(1,roleMapper.selectUserRoleNumbers(-9200002L,1L).size());
        assertEquals(List.of(-9200002L), userReader.selectEnabledExplicitRoleMembers(1L, ROLE_ID));
        assertTrue(userReader.selectEnabledExplicitRoleMembers(-9200004L, ROLE_ID).isEmpty());
        setDefault(-9200003L);
        assertTrue(permissionReader.selectUserPermissionNumbers(-9200002L,-9200004L,null).isEmpty());
        assertTrue(menuReader.selectUserMenus(-9200002L,-9200004L,31L,false).isEmpty());
        assertTrue(appReader.selectUserDomainApps(-9200002L,-9200004L).isEmpty());
        assertEquals(2,roleMapper.selectUserRoleNumbers(-9200002L,1L).size());
        assertEquals(List.of("verify-new-default"),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        var scopes=scopeReader.selectEffectiveRules(-9200002L,1L,"verify.default","VIEW");
        assertEquals(2,scopes.size());
        assertTrue(scopes.stream().anyMatch(rule -> "ORG".equals(rule.getScopeType())));
        setDefault(null);
        assertNull(defaultReader.selectConfiguration().getRole());
        // 后一角色仍有手工用户关系，外键拒绝必须回滚前一角色已完成的删除。
        assertThrows(RuntimeException.class, () -> service.deleteByIds(List.of(-9200003L, ROLE_ID)));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM t_sys_role WHERE id=-9200003",Integer.class));
        assertEquals(List.of("verify-role-lock"),roleMapper.selectUserRoleNumbers(-9200002L,1L));
        assertEquals(List.of(),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        setDefault(-9200003L);
        jdbc.update("UPDATE t_sys_org SET enabled=false WHERE id=-9200004");
        assertEquals(List.of(),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        jdbc.update("UPDATE t_sys_org SET enabled=false, archived=true, archived_at=CURRENT_TIMESTAMP WHERE id=-9200004");
        assertEquals(List.of(),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        jdbc.update("DELETE FROM t_sys_user_assignment WHERE user_id=-9200002 AND org_id=-9200004");
        assertEquals(List.of(),roleMapper.selectUserRoleNumbers(-9200002L,-9200004L));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM t_sys_user_role WHERE user_id=-9200002",Integer.class));
    }

    @Test
    void invalidAndRolledBackChangesDoNotAlterDefaultRole() {
        var before=defaultForm(ROLE_ID);
        var missing=defaultForm(-99999999L);
        assertThrows(BizException.class, () -> service.setDefault(missing));
        assertNull(jdbc.queryForObject("SELECT role_id FROM t_sys_role_default WHERE id=1",Long.class));
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            service.setDefault(before);
            throw new IllegalStateException("验证整体回滚");
        }));
        assertNull(jdbc.queryForObject("SELECT role_id FROM t_sys_role_default WHERE id=1",Long.class));
        service.setDefault(before);
        assertThrows(BizException.class, () -> service.setDefault(before));
        // 数据库自身限制单例且外键禁止绕过业务命令删除默认角色。
        assertThrows(RuntimeException.class, () -> jdbc.update("INSERT INTO t_sys_role_default(id,version) VALUES (2,0)"));
        assertThrows(RuntimeException.class, () -> jdbc.update("DELETE FROM t_sys_role WHERE id=?",ROLE_ID));
    }

    @Test
    void concurrentDefaultChangesRejectStaleSnapshotAfterLockWait() throws Exception {
        var firstWritten=new CountDownLatch(1);
        var allowCommit=new CountDownLatch(1);
        var secondStarted=new CountDownLatch(1);
        var backend=new AtomicInteger();
        var snapshot=defaultForm(ROLE_ID);
        var executor=Executors.newFixedThreadPool(2);
        try {
            var first=executor.submit(() -> transaction.executeWithoutResult(status -> {
                service.setDefault(snapshot);
                firstWritten.countDown();
                await(allowCommit);
            }));
            assertTrue(firstWritten.await(10,TimeUnit.SECONDS));
            var second=executor.submit(() -> transaction.executeWithoutResult(status -> {
                backend.set(jdbc.queryForObject("SELECT pg_backend_pid()",Integer.class));
                secondStarted.countDown();
                service.setDefault(snapshot);
            }));
            assertTrue(secondStarted.await(10,TimeUnit.SECONDS));
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            boolean blocked=false;
            while(System.nanoTime()<deadline && !second.isDone()) {
                blocked=Boolean.TRUE.equals(jdbc.queryForObject("SELECT cardinality(pg_blocking_pids(?))>0",Boolean.class,backend.get()));
                if(blocked) break;
                Thread.sleep(10);
            }
            assertTrue(blocked,"默认角色切换必须等待配置行锁");
            allowCommit.countDown();
            first.get(10,TimeUnit.SECONDS);
            var error=assertThrows(ExecutionException.class, () -> second.get(10,TimeUnit.SECONDS));
            assertInstanceOf(BizException.class,error.getCause());
            assertEquals(ROLE_ID,jdbc.queryForObject("SELECT role_id FROM t_sys_role_default WHERE id=1",Long.class));
        } finally {
            allowCommit.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15,TimeUnit.SECONDS));
        }
    }

    private void setDefault(Long roleId) { service.setDefault(defaultForm(roleId)); }

    private RoleDefaultSaveForm defaultForm(Long roleId) {
        var result=new RoleDefaultSaveForm();
        result.setRoleId(roleId);
        result.setPreviousRoleId(jdbc.queryForObject("SELECT role_id FROM t_sys_role_default WHERE id=1",Long.class));
        result.setVersion(jdbc.queryForObject("SELECT version FROM t_sys_role_default WHERE id=1",Integer.class));
        return result;
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
