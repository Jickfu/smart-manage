package sm.infrastructure.persistence;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.quartz.autoconfigure.QuartzDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DruidDataSourceConfigTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    DruidDataSourceAutoConfigure.class,
                    FlywayAutoConfiguration.class))
            .withUserConfiguration(
                    DruidDataSourceConfig.class,
                    TestQuartzDataSourceConfig.class,
                    TestFlywayConfig.class)
            .withPropertyValues(
                    "spring.datasource.type=com.alibaba.druid.pool.DruidDataSource",
                    "spring.datasource.url=jdbc:postgresql://localhost:5432/smart_manage",
                    "spring.datasource.username=postgres",
                    "spring.datasource.password=postgres",
                    "spring.datasource.druid.filters=stat",
                    "spring.datasource.druid.filter.stat.enabled=true",
                    "spring.datasource.druid.filter.stat.merge-sql=true",
                    "spring.datasource.druid.filter.stat.log-slow-sql=false",
                    "spring.datasource.druid.filter.stat.slow-sql-millis=1500");

    @Test
    void initializesPrimaryDataSourceWithConfiguredStatFilter() {
        contextRunner.run(context -> {
            DataSource defaultDataSource = context.getBean(DataSource.class);
            DruidDataSourceWrapper druidDataSource = context.getBean(DruidDataSourceWrapper.class);
            DruidDataSource quartzDataSource = context.getBean("quartzDataSource", DruidDataSource.class);
            SimpleDriverDataSource flywayDataSource =
                    context.getBean("flywayDataSource", SimpleDriverDataSource.class);

            assertSame(druidDataSource, defaultDataSource);
            assertEquals(3, context.getBeansOfType(DataSource.class).size());
            assertEquals(1, druidDataSource.getProxyFilters().size());
            StatFilter statFilter = (StatFilter) druidDataSource.getProxyFilters().getFirst();
            assertTrue(statFilter.isMergeSql());
            assertFalse(statFilter.isLogSlowSql());
            assertEquals(1500, statFilter.getSlowSqlMillis());
            assertTrue(quartzDataSource.getProxyFilters().isEmpty());
            assertEquals("jdbc:postgresql://localhost:5432/smart_manage", flywayDataSource.getUrl());
            assertSame(flywayDataSource, context.getBean(org.flywaydb.core.Flyway.class)
                    .getConfiguration()
                    .getDataSource());
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestQuartzDataSourceConfig {
        @Bean(defaultCandidate = false)
        @QuartzDataSource
        DruidDataSource quartzDataSource() {
            return new DruidDataSource();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestFlywayConfig {
        @Bean
        FlywayMigrationStrategy skipMigration() {
            return flyway -> {
                // 本测试只验证 Flyway 的数据源选择，不连接测试数据库。
            };
        }
    }
}
