package sm.domain.sys.base.weakpassword.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordSaveForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordDeleteForm;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 验证真实数据库唯一约束、乐观锁与跨会话策略生效，不依赖进程内缓存。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class WeakPasswordPostgresTests {
    private JdbcTemplate jdbc;
    private WeakPasswordMapper mapper;
    private WeakPasswordTxService service;
    private PasswordPolicyService firstPolicy;
    private PasswordPolicyService secondPolicy;
    private TransactionTemplate transaction;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var manager = new DataSourceTransactionManager(source);
        transaction = new TransactionTemplate(manager);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(WeakPasswordMapper.class);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(interceptor);
        var sessionFactory = factory.getObject();
        mapper = new SqlSessionTemplate(sessionFactory).getMapper(WeakPasswordMapper.class);
        firstPolicy = new PasswordPolicyService(mapper);
        secondPolicy = new PasswordPolicyService(new SqlSessionTemplate(sessionFactory).getMapper(WeakPasswordMapper.class));
        var proxy = new ProxyFactory(new WeakPasswordTxService(mapper));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        service = (WeakPasswordTxService) proxy.getProxy();
    }

    @AfterEach
    void cleanUp() {
        for (Long id : createdIds) jdbc.update("DELETE FROM t_sys_weak_password WHERE id = ?", id);
    }

    @Test
    void suppliedSeedHasMatchingDigestsAndShortWordsRemainAvailableToMaintain() {
        var rows = jdbc.queryForList("SELECT word, match_digest FROM t_sys_weak_password WHERE id BETWEEN 560000000000010000 AND 560000000000011001");
        assertEquals(1002, rows.size());
        for (var row : rows) {
            assertEquals(WeakPasswordUtil.digest((String) row.get("word")), row.get("match_digest"));
        }
        assertTrue(rows.stream().anyMatch(row -> ((String) row.get("word")).length() < 15));
    }

    @Test
    void savesEditsAndDeletesAreImmediatelySeenByOtherSessionsAndStaleCommandsFail() {
        String original = "integration river moonlight";
        String replacement = "integration forest sunshine";
        assertDoesNotThrow(() -> firstPolicy.validate(original, "tester"));
        Long id = saveNew(original);
        assertThrows(BizException.class, () -> secondPolicy.validate(original.toUpperCase(java.util.Locale.ROOT), "tester"));
        var current = mapper.selectById(id);
        var edit = new WeakPasswordSaveForm();
        edit.setId(id);
        edit.setVersion(current.getVersion());
        edit.setWord(replacement);
        service.save(edit);
        assertDoesNotThrow(() -> firstPolicy.validate(original, "tester"));
        assertThrows(BizException.class, () -> firstPolicy.validate(replacement, "tester"));
        assertThrows(BizException.class, () -> service.save(edit));
        var delete = new WeakPasswordDeleteForm();
        delete.setId(id);
        delete.setVersion(current.getVersion());
        assertThrows(BizException.class, () -> service.delete(delete));
        delete.setVersion(mapper.selectById(id).getVersion());
        service.delete(delete);
        assertDoesNotThrow(() -> secondPolicy.validate(replacement, "tester"));
    }

    @Test
    void normalizedDuplicatesAreRejectedAndTransactionRollbackRestoresPolicy() {
        String word = "integration café moonlight";
        Long id = saveNew(word);
        var duplicate = new WeakPasswordSaveForm();
        duplicate.setWord("INTEGRATION CAFE\u0301 MOONLIGHT");
        assertThrows(BizException.class, () -> service.save(duplicate));
        assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> jdbc.update(
                "INSERT INTO t_sys_weak_password (id, word, match_digest) VALUES (?, ?, ?)",
                -id, "another word", WeakPasswordUtil.digest(word)));
        transaction.executeWithoutResult(status -> {
            var delete = new WeakPasswordDeleteForm();
            delete.setId(id);
            delete.setVersion(mapper.selectById(id).getVersion());
            service.delete(delete);
            assertDoesNotThrow(() -> firstPolicy.validate(word, "tester"));
            status.setRollbackOnly();
        });
        assertThrows(BizException.class, () -> secondPolicy.validate(word, "tester"));
    }

    private Long saveNew(String word) {
        var form = new WeakPasswordSaveForm();
        form.setWord(word);
        Long id = service.save(form);
        createdIds.add(id);
        return id;
    }
}
