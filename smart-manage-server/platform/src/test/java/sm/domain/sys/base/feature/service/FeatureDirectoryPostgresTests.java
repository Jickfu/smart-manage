package sm.domain.sys.base.feature.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import sm.domain.sys.base.feature.mapper.FeatureMapper;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 在平台自身装配内验证通用目录契约，不依赖工作流或其他可选领域。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class FeatureDirectoryPostgresTests {
    private static final long DOMAIN_ID = -9500001L;
    private static final long APP_ID = -9500002L;
    private static final long FEATURE_ID = -9500003L;
    private JdbcTemplate jdbc;
    private FeatureDirectoryService service;

    @BeforeEach
    void initialize() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(source);
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(FeatureMapper.class);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new ClassPathResource("mapper/common/ListSqlQueryMapper.xml"),
                new ClassPathResource("mapper/sys/base/feature/FeatureMapper.xml"));
        service = new FeatureDirectoryService(new SqlSessionTemplate(factory.getObject()).getMapper(FeatureMapper.class));
        jdbc.update("INSERT INTO t_sys_domain(id, number, name, seq) VALUES (?, 'verify-directory', '目录领域', 99)", DOMAIN_ID);
        jdbc.update("INSERT INTO t_sys_app(id, number, name, icon, domain_id, seq) VALUES (?, 'verify-directory', '目录应用', 'AppstoreOutlined', ?, 99)", APP_ID, DOMAIN_ID);
        jdbc.update("INSERT INTO t_sys_feature(id, feature_key, app_id, default_name) VALUES (?, 'unrelated/path/document', ?, '测试功能')", FEATURE_ID, APP_ID);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM t_sys_feature WHERE id=?", FEATURE_ID);
        jdbc.update("DELETE FROM t_sys_app WHERE id=?", APP_ID);
        jdbc.update("DELETE FROM t_sys_domain WHERE id=?", DOMAIN_ID);
    }

    @Test
    void readsActualForeignKeysAndCurrentNamesWithoutInferringPrefixes() {
        var directory = service.findByFeatureKeys(List.of("unrelated/path/document", "unrelated/path/document"));
        assertEquals(1, directory.size());
        var entry = directory.getFirst();
        assertEquals(DOMAIN_ID, entry.domainId());
        assertEquals(APP_ID, entry.appId());
        assertEquals("目录领域", entry.domainName());
        assertEquals("目录应用", entry.appName());
        jdbc.update("UPDATE t_sys_app SET name='更新后的应用名' WHERE id=?", APP_ID);
        assertEquals("更新后的应用名", service.findByFeatureKeys(List.of(entry.featureKey())).getFirst().appName());
    }

    @Test
    void onlyRequestedFeaturesAreReturnedAndEmptyKeysNeverMeanAllFeatures() {
        assertTrue(service.findByFeatureKeys(List.of()).isEmpty());
        assertTrue(service.findByFeatureKeys(List.of("missing-feature")).isEmpty());
        assertTrue(service.findByFeatureKeys(List.of("' OR 1=1 --")).isEmpty());
        assertEquals(1, service.findByFeatureKeys(List.of("unrelated/path/document", "missing-feature")).size());
    }
}
