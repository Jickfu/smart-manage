package sm.domain.sys.base.passwordconfig.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.passwordconfig.mapper.PasswordConfigMapper;
import sm.domain.sys.base.passwordconfig.converter.PasswordConfigConverter;
import sm.domain.sys.base.passwordconfig.model.form.PasswordConfigSaveForm;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.exception.BizException;
import static org.junit.jupiter.api.Assertions.*;

/** 验证数据库约束、真实乐观锁、事务回滚及不同实例/同一会话策略新鲜度。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class PasswordConfigPostgresTests {
    private JdbcTemplate jdbc;
    private PasswordConfigMapper mapper;
    private PasswordConfigService reader;
    private PasswordConfigTxService writer;
    private PasswordPolicyService policy;
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
        configuration.addMapper(PasswordConfigMapper.class);
        configuration.addMapper(WeakPasswordMapper.class);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(interceptor);
        var sessionFactory = factory.getObject();
        mapper = new SqlSessionTemplate(sessionFactory).getMapper(PasswordConfigMapper.class);
        var proxy = new ProxyFactory(new PasswordConfigTxService(mapper));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        writer = (PasswordConfigTxService) proxy.getProxy();
        var otherMapper = new SqlSessionTemplate(sessionFactory).getMapper(PasswordConfigMapper.class);
        reader = new PasswordConfigService(otherMapper, writer,
                org.mapstruct.factory.Mappers.getMapper(PasswordConfigConverter.class));
        policy = new PasswordPolicyService(new SqlSessionTemplate(sessionFactory).getMapper(WeakPasswordMapper.class), reader);
    }

    @AfterEach
    void restoreDefault() {
        jdbc.update("UPDATE t_sys_password_config SET min_length = 15, complexity_enabled = false, version = 0 WHERE id = 1");
    }

    @Test
    void defaultsAndCommittedChangesAreVisibleAcrossReadersAndStaleSaveFails() {
        assertEquals(15, reader.policy().minLength());
        assertFalse(reader.policy().complexityEnabled());
        var stale = form(4, true);
        writer.save(form(4, true));
        assertEquals(4, reader.policy().minLength());
        assertTrue(reader.policy().complexityEnabled());
        assertDoesNotThrow(() -> policy.validate("Ab7!", "tester"));
        assertThrows(BizException.class, () -> policy.validate("abcd", "tester"));
        assertThrows(BizException.class, () -> writer.save(stale));
        writer.save(form(64, true));
        assertEquals(64, policy.generate("tester").length());
    }

    @Test
    void rollbackDoesNotPublishPolicyAndRepeatedReadsDoNotReuseSessionCache() {
        transaction.executeWithoutResult(status -> {
            writer.save(form(4, true));
            assertEquals(4, reader.policy().minLength());
            status.setRollbackOnly();
        });
        assertEquals(15, reader.policy().minLength());
        transaction.executeWithoutResult(status -> {
            assertEquals(15, reader.policy().minLength());
            // 独立连接模拟另一个实例提交；当前事务后续 SELECT 必须读到新规则。
            java.util.concurrent.CompletableFuture.runAsync(() -> writer.save(form(4, false))).join();
            assertEquals(4, reader.policy().minLength());
        });
    }

    @Test
    void databaseRejectsInvalidLengthsAndAdditionalSingletonRows() {
        for (int invalidLength : new int[]{0, 3, 65}) {
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                    () -> jdbc.update("UPDATE t_sys_password_config SET min_length = ? WHERE id = 1", invalidLength));
            assertThrows(BizException.class, () -> writer.save(form(invalidLength, true)));
        }
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("INSERT INTO t_sys_password_config(id, min_length, complexity_enabled) VALUES (2, 8, true)"));
    }

    private PasswordConfigSaveForm form(int minLength, boolean complexityEnabled) {
        var form = new PasswordConfigSaveForm();
        form.setId(1L);
        form.setVersion(mapper.current().getVersion());
        form.setMinLength(minLength);
        form.setComplexityEnabled(complexityEnabled);
        return form;
    }
}
