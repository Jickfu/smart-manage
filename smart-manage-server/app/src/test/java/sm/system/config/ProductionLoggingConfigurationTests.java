package sm.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionLoggingConfigurationTests {

    private static final String JETCACHE_STAT_LOGGER =
            "logging.level.com.alicp.jetcache.support.StatInfoLogger";
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void onlyProductionMustDisableJetCachePeriodicStatisticsLog() throws IOException {
        PropertySource<?> production = loadSingle("application-prod.yml");

        assertThat(production.getProperty(JETCACHE_STAT_LOGGER)).isEqualTo("OFF");
        assertThat(production.getProperty("jetcache.statIntervalMinutes"))
                .as("生产环境仍需采集 JetCache 统计供缓存监控使用")
                .isEqualTo(15);

        for (String resourceName : List.of("application-dev.yml", "application-test.yml")) {
            assertThat(loadSingle(resourceName).getProperty(JETCACHE_STAT_LOGGER))
                    .as(resourceName + " 必须保持 JetCache 周期统计日志")
                    .isNull();
        }
    }

    private PropertySource<?> loadSingle(String resourceName) throws IOException {
        List<PropertySource<?>> sources = loader.load(resourceName, new ClassPathResource(resourceName));
        assertThat(sources).as(resourceName + " 必须只包含一个 YAML 文档").hasSize(1);
        return sources.getFirst();
    }
}
