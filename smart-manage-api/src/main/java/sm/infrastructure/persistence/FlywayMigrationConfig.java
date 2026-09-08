package sm.infrastructure.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 沿用 Boot 的平台 Flyway 与数据库初始化依赖，只替换有序执行策略。 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(name = "spring.flyway.enabled", matchIfMissing = true)
@EnableConfigurationProperties(BusinessMigrationProperties.class)
public class FlywayMigrationConfig {
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(BusinessMigrationProperties properties) {
        return new DualFlywayMigrationStrategy(properties);
    }
}
