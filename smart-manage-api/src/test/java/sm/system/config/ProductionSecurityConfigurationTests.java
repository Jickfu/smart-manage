package sm.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionSecurityConfigurationTests {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void commonAndProductionConfigurationMustBothDisableSaTokenCredentialLogging() throws IOException {
        PropertySource<?> common = loadSingle("application.yml");
        PropertySource<?> production = loadSingle("application-prod.yml");

        assertThat(common.getProperty("sa-token.is-log")).isEqualTo(false);
        assertThat(production.getProperty("sa-token.is-log")).isEqualTo(false);
    }

    @Test
    void productionOverridesMustResolveToSecureBrowserAuthenticationSettings() throws IOException {
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(loadSingle("application-prod.yml"));
        sources.addLast(loadSingle("application.yml"));
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);

        assertThat(resolver.getProperty("sa-token.is-log", Boolean.class)).isFalse();
        assertThat(resolver.getProperty("sa-token.cookie.secure", Boolean.class)).isTrue();
        assertThat(resolver.getProperty("sa-token.is-read-header", Boolean.class)).isFalse();
        assertThat(resolver.getProperty("sa-token.is-read-body", Boolean.class)).isFalse();
    }

    private PropertySource<?> loadSingle(String resourceName) throws IOException {
        List<PropertySource<?>> sources = loader.load(resourceName, new ClassPathResource(resourceName));
        assertThat(sources).as(resourceName + " 必须只包含一个 YAML 文档").hasSize(1);
        return sources.getFirst();
    }
}
