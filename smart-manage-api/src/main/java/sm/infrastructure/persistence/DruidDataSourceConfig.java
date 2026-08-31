package sm.infrastructure.persistence;

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceWrapper;
import org.springframework.boot.flyway.autoconfigure.FlywayDataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

/** 主 Druid 数据源配置，显式隔离 Quartz 数据源并保留 Druid Starter 的完整初始化生命周期。 */
@Configuration(proxyBeanMethods = false)
public class DruidDataSourceConfig {
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.druid")
    DruidDataSourceWrapper dataSource() {
        return new DruidDataSourceWrapper();
    }

    /** Flyway 直接使用 JDBC 连接，避免 Druid StatFilter 尝试合并 PostgreSQL DDL 并刷出解析异常。 */
    @Bean(defaultCandidate = false)
    @FlywayDataSource
    DataSource flywayDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(SimpleDriverDataSource.class)
                .build();
    }
}
