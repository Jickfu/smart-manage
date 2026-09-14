package sm.system.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPathConfigurationTests {
    private static final String INTERNAL_BASE_URL =
            "smart-manage.domain.sys.monitor.cluster.internal-base-url";

    @ParameterizedTest
    @ValueSource(strings = {"dev", "test"})
    void localInternalAddressFollowsPortAndContextPath(String profile) throws IOException {
        MockEnvironment environment = loadEnvironment(profile);
        URI defaultAddress = URI.create(environment.getRequiredProperty(INTERNAL_BASE_URL));
        assertThat(defaultAddress.getHost()).isEqualTo("127.0.0.1");
        assertThat(defaultAddress.getPort()).isEqualTo(environment.getRequiredProperty("server.port", Integer.class));
        assertThat(defaultAddress.getPath()).isEqualTo(environment.getProperty("server.servlet.context-path", ""));

        environment.setProperty("server.port", "9080");
        environment.setProperty("server.servlet.context-path", "/custom/api");
        assertThat(environment.getProperty(INTERNAL_BASE_URL))
                .isEqualTo("http://127.0.0.1:9080/custom/api");

        environment.setProperty("server.servlet.context-path", "");
        assertThat(environment.getProperty(INTERNAL_BASE_URL)).isEqualTo("http://127.0.0.1:9080");
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "test", "prod"})
    void explicitInternalAddressRemainsIndependentOfListeningAddress(String profile) throws IOException {
        MockEnvironment environment = loadEnvironment(profile);
        environment.setProperty("server.port", "9080");
        environment.setProperty("server.servlet.context-path", "/custom/api");
        environment.setProperty("SMART_MANAGE_INTERNAL_BASE_URL", "https://node.example.com:9443/custom/api");
        assertThat(environment.getProperty(INTERNAL_BASE_URL))
                .isEqualTo("https://node.example.com:9443/custom/api");
    }

    private MockEnvironment loadEnvironment(String profile) throws IOException {
        MockEnvironment environment = new MockEnvironment();
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        for (String resourceName : new String[]{"application-" + profile + ".yml", "application.yml"}) {
            environment.getPropertySources().addLast(
                    loader.load(resourceName, new ClassPathResource(resourceName)).getFirst());
        }
        return environment;
    }
}
