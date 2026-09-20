package sm.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SmartManageConfigurationNamespaceTests {

    private static final Set<String> ALLOWED_LAYERS = Set.of("infrastructure", "system", "domain");
    private static final Set<String> ENVIRONMENT_PROPERTY_PREFIXES = Set.of(
            "spring.lifecycle.",
            "spring.web.resources.cache.",
            "spring.servlet.multipart.",
            "spring.task.execution.pool.",
            "spring.task.scheduling.pool.",
            "spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval",
            "spring.quartz.properties.org.quartz.jobStore.misfireThreshold",
            "spring.quartz.properties.org.quartz.threadPool.threadCount",
            "server.port",
            "server.compression.",
            "server.tomcat.",
            "logging.file.",
            "logging.logback.",
            "jetcache.statIntervalMinutes",
            "jetcache.local.default.limit",
            "jetcache.local.default.expireAfterWriteInMillis",
            "sa-token.timeout",
            "sa-token.active-timeout",
            "sa-token.cookie.secure",
            "smart-manage.infrastructure.http.",
            "smart-manage.system.runtime.",
            "smart-manage.system.security.argon2.",
            "smart-manage.system.log.core-pool-size",
            "smart-manage.system.log.max-pool-size",
            "smart-manage.system.log.queue-capacity",
            "smart-manage.system.log.keep-alive-seconds",
            "smart-manage.domain.");
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void projectConfigurationMustUseArchitectureLayerAsFirstLevelNamespace() throws IOException {
        for (String resourceName : List.of("application.yml", "application-test.yml", "application-prod.yml")) {
            PropertySource<?> source = loadSingle(resourceName);
            assertThat(source).isInstanceOf(EnumerablePropertySource.class);
            String[] propertyNames = ((EnumerablePropertySource<?>) source).getPropertyNames();

            // smart-manage 下只允许架构层级，避免领域或具体能力绕过统一命名空间。
            List<String> invalidProperties = Arrays.stream(propertyNames)
                    .filter(propertyName -> propertyName.startsWith("smart-manage."))
                    .filter(propertyName -> !belongsToAllowedLayer(propertyName))
                    .toList();

            assertThat(invalidProperties)
                    .as(resourceName + " 的 smart-manage 配置必须归入 infrastructure、system 或 domain")
                    .isEmpty();
        }
    }

    @Test
    void commonConfigurationMustNotContainEnvironmentProperties() throws IOException {
        String[] propertyNames = propertyNames(loadSingle("application.yml"));

        // 公共文件只保存环境不变量；即使各环境当前取值相同，部署参数也必须留在环境配置中。
        List<String> environmentProperties = Arrays.stream(propertyNames)
                .filter(this::isEnvironmentProperty)
                .toList();

        assertThat(environmentProperties)
                .as("application.yml 不得包含具有部署属性的配置")
                .isEmpty();
    }

    @Test
    void profileConfigurationMustExposeCoreDeploymentContract() throws IOException {
        for (String resourceName : List.of("application-test.yml", "application-prod.yml")) {
            assertThat(propertyNames(loadSingle(resourceName)))
                    .as(resourceName + " 必须显式提供核心部署配置")
                    .contains(
                            "server.port",
                            "spring.servlet.multipart.max-file-size",
                            "sa-token.cookie.secure",
                            "smart-manage.system.runtime.instance-id",
                            "smart-manage.system.security.argon2.iterations",
                            "smart-manage.domain.sys.monitor.sampling.interval-ms");
        }
    }

    @Test
    void allProfilesMustDisableActuatorWebExposure() throws IOException {
        for (String resourceName : List.of(
                "application-dev.yml", "application-test.yml", "application-prod.yml")) {
            PropertySource<?> source = loadSingle(resourceName);

            // 内建监控直接使用 Actuator Bean，禁止为此开放任何 Actuator HTTP 入口。
            assertThat(source.getProperty("management.endpoints.web.exposure.exclude"))
                    .as(resourceName + " 必须禁止 Actuator Web 暴露")
                    .isEqualTo("*");
            assertThat(source.getProperty("management.endpoints.web.exposure.include"))
                    .as(resourceName + " 不得重新声明 Actuator Web 暴露白名单")
                    .isNull();
        }
    }

    private boolean belongsToAllowedLayer(String propertyName) {
        String remainingPath = propertyName.substring("smart-manage.".length());
        int separatorIndex = remainingPath.indexOf('.');
        String layer = separatorIndex < 0 ? remainingPath : remainingPath.substring(0, separatorIndex);
        return ALLOWED_LAYERS.contains(layer);
    }

    private boolean isEnvironmentProperty(String propertyName) {
        for (String prefix : ENVIRONMENT_PROPERTY_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String[] propertyNames(PropertySource<?> source) {
        assertThat(source).isInstanceOf(EnumerablePropertySource.class);
        return ((EnumerablePropertySource<?>) source).getPropertyNames();
    }

    private PropertySource<?> loadSingle(String resourceName) throws IOException {
        List<PropertySource<?>> sources = loader.load(resourceName, new ClassPathResource(resourceName));
        assertThat(sources).as(resourceName + " 必须只包含一个 YAML 文档").hasSize(1);
        return sources.getFirst();
    }
}
