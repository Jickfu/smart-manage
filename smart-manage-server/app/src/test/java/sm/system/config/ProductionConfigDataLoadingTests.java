package sm.system.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** 通过真实 ConfigData 加载器保护部署覆盖，避免误把外部文件当作完整公共配置。 */
class ProductionConfigDataLoadingTests {

    @TempDir
    Path deploymentDirectory;

    @Test
    void externalProductionFileMustRetainPackagedCommonAndProductionSettings() throws IOException {
        Files.writeString(deploymentDirectory.resolve("application-prod.yml"), """
                server:
                  port: 8441
                smart-manage:
                  system:
                    runtime:
                      instance-id: node-a
                """);

        StandardEnvironment environment = loadConfiguration("--spring.profiles.active=prod");

        assertProductionLayers(environment);
        assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(8441);
        assertThat(environment.getProperty("smart-manage.system.runtime.instance-id")).isEqualTo("node-a");
    }

    @Test
    void externalCommonFileCanSelectProductionAndProductionFileOverridesIt() throws IOException {
        Files.writeString(deploymentDirectory.resolve("application.yml"), """
                spring:
                  profiles:
                    active: prod
                  datasource:
                    druid:
                      max-active: 7
                server:
                  port: 9000
                """);
        Files.writeString(deploymentDirectory.resolve("application-prod.yml"), """
                server:
                  port: 8442
                """);

        StandardEnvironment environment = loadConfiguration();

        assertProductionLayers(environment);
        assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(8442);
        assertThat(environment.getProperty("spring.datasource.druid.max-active", Integer.class)).isEqualTo(7);
    }

    @Test
    void commandLineProfileAndPortOverrideExternalCommonSelection() throws IOException {
        Files.writeString(deploymentDirectory.resolve("application.yml"), """
                spring:
                  profiles:
                    active: dev
                server:
                  port: 9000
                """);

        StandardEnvironment environment = loadConfiguration(
                "--spring.profiles.active=prod", "--server.port=9443");

        assertProductionLayers(environment);
        assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(9443);
    }

    private StandardEnvironment loadConfiguration(String... overrides) {
        StandardEnvironment environment = new StandardEnvironment();
        // 测试隔离机器环境及工作目录；显式保留真实 classpath，并把临时外部目录放在后面。
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        String[] arguments = new String[overrides.length + 1];
        arguments[0] = "--spring.config.location=classpath:/," + deploymentDirectory.toUri();
        System.arraycopy(overrides, 0, arguments, 1, overrides.length);
        environment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(arguments));
        ConfigDataEnvironmentPostProcessor.applyTo(environment);
        return environment;
    }

    private void assertProductionLayers(StandardEnvironment environment) {
        assertThat(environment.getActiveProfiles()).containsExactly("prod");
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("smart-manage");
        assertThat(environment.getProperty("server.servlet.context-path")).isEqualTo("/smart-manage-api");
        assertThat(environment.getProperty("spring.quartz.properties.org.quartz.jobStore.isClustered", Boolean.class))
                .isTrue();
        assertThat(environment.getProperty("sa-token.cookie.secure", Boolean.class)).isTrue();
        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isFalse();
    }
}
