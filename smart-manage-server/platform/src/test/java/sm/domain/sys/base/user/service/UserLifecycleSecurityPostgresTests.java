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
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.helper.UserAuthorizationAccessor;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.user.constant.UserPermission;
import sm.domain.sys.base.user.converter.UserConverter;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 使用真实授权查询、用户 Mapper 和 Spring 事务验证委派权限不能接管保留账号。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class UserLifecycleSecurityPostgresTests {
    private static final long DELEGATE_ID = 9200000201L;
    private static final long ROLE_ID = 9200000202L;
    private JdbcTemplate jdbc;
    private UserMapper mapper;
    private UserService service;
    private UserTxService transaction;
    private UserWriter writer;
    private TransactionTemplate databaseTransaction;
    private Long orgId;

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var manager = new DataSourceTransactionManager(source);
        databaseTransaction = new TransactionTemplate(manager);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(UserMapper.class);
        configuration.addMapper(UserRoleMapper.class);
        configuration.addMapper(UserAssignmentMapper.class);
        configuration.addMapper(PermissionMapper.class);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(interceptor);
        factory.setMapperLocations(new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"),
                new ClassPathResource("mapper/sys/base/user/UserMapper.xml"),
                new ClassPathResource("mapper/sys/base/permission/PermissionMapper.xml"));
        var session = new SqlSessionTemplate(factory.getObject());
        mapper = session.getMapper(UserMapper.class);
        var roles = session.getMapper(UserRoleMapper.class);
        var assignments = session.getMapper(UserAssignmentMapper.class);
        var organizations = mock(OrgReferenceReader.class);
        var identity = mock(CurrentUserContext.class);
        when(identity.getUserId()).thenReturn(DELEGATE_ID);
        orgId = jdbc.queryForObject("SELECT org_id FROM t_sys_user_assignment WHERE user_id=1 AND is_primary=true", Long.class);
        when(identity.getOrgId()).thenReturn(orgId);
        when(identity.isAdministrator()).thenReturn(false);
        var policy = mock(PasswordPolicyService.class);
        writer = new UserWriter(mapper, roles, assignments, organizations, policy);
        var target = new UserTxService(mapper, roles, assignments, organizations, identity, writer,
                policy, mock(AttachmentGateway.class));
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        transaction = (UserTxService) proxy.getProxy();
        service = new UserService(mapper, assignments, organizations, mock(AttachmentService.class),
                transaction, mock(UserCacheInvalidator.class), mock(UserConverter.class), identity);
        jdbc.update("INSERT INTO t_sys_user(id, username, name, number, password, enabled) VALUES (?, 'lifecycle-delegate', '委派管理员', 'lifecycle-delegate', '', true)", DELEGATE_ID);
        jdbc.update("INSERT INTO t_sys_role(id, name, number) VALUES (?, '用户生命周期测试', 'lifecycle-delegate-role')", ROLE_ID);
        jdbc.update("INSERT INTO t_sys_user_role(id, user_id, org_id, role_id) VALUES (?, ?, ?, ?)", ROLE_ID, DELEGATE_ID, orgId, ROLE_ID);
        jdbc.update("""
                INSERT INTO t_sys_role_perms(id, role_id, permission_id)
                SELECT ? + row_number() OVER (ORDER BY id), ?, id FROM t_sys_permission
                WHERE number IN (?, ?, ?, ?)
                """, ROLE_ID, ROLE_ID, UserPermission.SAVE, UserPermission.DELETE, UserPermission.ENABLE, UserPermission.DISABLE);
        var loader = new StpInterfaceImpl(identity, new UserAuthorizationAccessor(
                session.getMapper(PermissionMapper.class), mock(RoleMapper.class)));
        var permissions = loader.getPermissionList(DELEGATE_ID, "login");
        assertTrue(permissions.containsAll(List.of(UserPermission.SAVE, UserPermission.DELETE,
                UserPermission.ENABLE, UserPermission.DISABLE)));
        assertFalse(permissions.contains("*"));
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM t_sys_role_perms WHERE role_id=?", ROLE_ID);
        jdbc.update("DELETE FROM t_sys_user_role WHERE user_id=?", DELEGATE_ID);
        jdbc.update("DELETE FROM t_sys_role WHERE id=?", ROLE_ID);
        jdbc.update("DELETE FROM t_sys_user WHERE id=?", DELEGATE_ID);
    }

    @Test
    void delegatedCrudCannotChangeAdministratorOrAnyMemberOfMixedBatch() {
        var before = administratorSnapshot();
        assertDenied(() -> service.save(form(null, "administrator")));
        assertDenied(() -> service.save(form(1L, "administrator")));
        // 空任职和伪造用户名也必须按数据库目标拒绝，不能停用或移除管理员主职。
        assertDenied(() -> service.save(form(1L, "forged-name")));
        assertDenied(() -> service.disable(List.of(DELEGATE_ID, 1L)));
        assertDenied(() -> service.enable(List.of(DELEGATE_ID, 1L)));
        assertDenied(() -> service.deleteById(1L));
        assertDenied(() -> service.save(form(null, "administrator")));
        assertEquals(before, administratorSnapshot());
        assertTrue(mapper.selectById(DELEGATE_ID).getEnabled());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM t_sys_user WHERE username='administrator'", Integer.class));
    }

    @Test
    void transactionAndImportWriterCannotBypassPublicServiceGuard() {
        var before = administratorSnapshot();
        assertDenied(() -> transaction.save(form(null, "administrator")));
        assertDenied(() -> transaction.save(form(1L, "forged-name"), 1L));
        assertDenied(() -> transaction.deleteById(1L));
        assertDenied(() -> transaction.updateEnabled(List.of(DELEGATE_ID, 1L), false));
        assertDenied(() -> transaction.updateEnabled(List.of(DELEGATE_ID, 1L), true));
        assertDenied(() -> databaseTransaction.executeWithoutResult(status -> writer.save(form(1L, "forged-name"), 1L)));
        assertEquals(before, administratorSnapshot());
    }

    @Test
    void reservedNameCannotBeRecreatedEvenWhenAdministratorRowIsAbsent() {
        // 仅在回滚事务内模拟账号缺失，确保保留名保护不依赖重复用户名检查。
        var before = administratorSnapshot();
        databaseTransaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM t_sys_user_role WHERE user_id=1");
            jdbc.update("DELETE FROM t_sys_user_assignment WHERE user_id=1");
            jdbc.update("DELETE FROM t_sys_user WHERE id=1");
            assertDenied(() -> service.save(form(null, "administrator")));
            status.setRollbackOnly();
        });
        assertEquals(before, administratorSnapshot());
    }

    @Test
    void ordinaryUserEditingAndDeletionRemainAvailable() {
        var existing = mapper.selectById(DELEGATE_ID);
        var form = form(DELEGATE_ID, existing.getUsername());
        form.setNumber(existing.getNumber());
        form.setVersion(existing.getVersion());
        service.save(form);
        assertFalse(mapper.selectById(DELEGATE_ID).getEnabled());
        // 保留既有禁止删除当前用户规则，测试另一操作者的正常删除。
        databaseTransaction.executeWithoutResult(status -> {
            writer.save(form(null, "ordinary-lifecycle-test"), ROLE_ID);
            service.deleteById(ROLE_ID);
            assertNull(mapper.selectById(ROLE_ID));
            status.setRollbackOnly();
        });
    }

    private Object administratorSnapshot() {
        return List.of(jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id=1"),
                jdbc.queryForList("SELECT * FROM t_sys_user_assignment WHERE user_id=1 ORDER BY id"),
                jdbc.queryForList("SELECT * FROM t_sys_user_role WHERE user_id=1 ORDER BY id"));
    }

    private UserSaveForm form(Long id, String username) {
        var form = new UserSaveForm();
        form.setId(id);
        form.setUsername(username);
        form.setName("生命周期测试用户");
        form.setNumber("lifecycle-new-user");
        form.setPassword(id == null ? "lifecycle-test-password" : null);
        if (id != null) form.setVersion(mapper.selectById(id).getVersion());
        return form;
    }

    private void assertDenied(Runnable command) {
        assertEquals(ResultEnum.PERMISSION_ERROR.getCode(), assertThrows(BizException.class, command::run).getCode());
    }
}
