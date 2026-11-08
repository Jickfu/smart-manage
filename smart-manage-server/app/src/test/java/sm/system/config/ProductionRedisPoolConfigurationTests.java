package sm.system.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** 使用实际绑定器保护 JetCache 启动时要求的连接池配置。 */
class ProductionRedisPoolConfigurationTests {

    @ParameterizedTest
    @ValueSource(strings = {"dev", "test", "prod"})
    void redisPoolMustBindForEveryDeploymentProfile(String profile) throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader()
                .load(profile, new ClassPathResource("application-" + profile + ".yml"))
                .forEach(environment.getPropertySources()::addFirst);

        GenericObjectPoolConfig<?> pool = Binder.get(environment)
                .bind("jetcache.remote.default.pool-config", GenericObjectPoolConfig.class)
                .get();

        assertThat(pool.getMaxTotal()).isPositive();
        assertThat(pool.getMaxIdle()).isBetween(pool.getMinIdle(), pool.getMaxTotal());
        assertThat(pool.getMinIdle()).isNotNegative();
    }
}
