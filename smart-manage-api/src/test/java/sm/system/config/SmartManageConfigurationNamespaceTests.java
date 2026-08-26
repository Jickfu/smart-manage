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
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void projectConfigurationMustUseArchitectureLayerAsFirstLevelNamespace() throws IOException {
        for (String resourceName : List.of("application.yml", "application-dev.yml", "application-prod.yml")) {
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

    private boolean belongsToAllowedLayer(String propertyName) {
        String remainingPath = propertyName.substring("smart-manage.".length());
        int separatorIndex = remainingPath.indexOf('.');
        String layer = separatorIndex < 0 ? remainingPath : remainingPath.substring(0, separatorIndex);
        return ALLOWED_LAYERS.contains(layer);
    }

    private PropertySource<?> loadSingle(String resourceName) throws IOException {
        List<PropertySource<?>> sources = loader.load(resourceName, new ClassPathResource(resourceName));
        assertThat(sources).as(resourceName + " 必须只包含一个 YAML 文档").hasSize(1);
        return sources.getFirst();
    }
}
