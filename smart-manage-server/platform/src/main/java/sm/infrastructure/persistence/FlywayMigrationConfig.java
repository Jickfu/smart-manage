package sm.infrastructure.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;

/** 保留 Boot 初始化依赖：平台先迁移，再执行已装配领域的独立版本链。 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(name = "spring.flyway.enabled", matchIfMissing = true)
public class FlywayMigrationConfig {
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() throws IOException {
        var declarations = new ArrayList<DomainMigration>();
        var identities = new HashSet<String>();
        var tables = new HashSet<String>();
        var locations = new HashSet<String>();
        for (var resource : new PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/smart-manage/migration.properties")) {
            var properties = new Properties();
            try (var input = resource.getInputStream()) { properties.load(input); }
            var declaration = new DomainMigration(properties.getProperty("id"), properties.getProperty("location"),
                    properties.getProperty("table"), properties.getProperty("minimum-platform-version"));
            if (!identities.add(declaration.id()) || !tables.add(declaration.table())
                    || !locations.add(declaration.location())) {
                throw new IllegalArgumentException("领域迁移标识、历史表或目录重复");
            }
            declarations.add(declaration);
        }
        // 当前可选领域仅依赖平台；按标识稳定排序，不表达可选领域之间的依赖关系。
        declarations.sort(Comparator.comparing(DomainMigration::id));
        return platform -> {
            if (declarations.isEmpty()) { platform.migrate(); return; }
            for (var declaration : declarations) {
                new DomainFlywayMigrationStrategy(declaration).migrate(platform);
            }
        };
    }
}
