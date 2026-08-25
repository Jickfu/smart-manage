package sm.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LogThreadPoolConfigTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> ConfigurationPropertiesBindingPostProcessor.register(
                    (BeanDefinitionRegistry) context.getBeanFactory()))
            .withUserConfiguration(LogThreadPoolConfig.class, TraceIdTaskDecorator.class)
            .withPropertyValues(
                    "smart-manage.system.log.core-pool-size=10",
                    "smart-manage.system.log.max-pool-size=20",
                    "smart-manage.system.log.queue-capacity=1000",
                    "smart-manage.system.log.keep-alive-seconds=60",
                    "smart-manage.system.log.thread-name-prefix=biz-log-");

    @Test
    void bindsSystemLogPropertiesBeforeInitializingExecutor() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            ThreadPoolTaskExecutor taskExecutor = context.getBean("logTaskExecutor", ThreadPoolTaskExecutor.class);
            assertEquals(10, taskExecutor.getCorePoolSize());
            assertEquals(20, taskExecutor.getMaxPoolSize());
            assertEquals(1000, taskExecutor.getQueueCapacity());
            assertEquals(60, taskExecutor.getKeepAliveSeconds());
            assertEquals("biz-log-", taskExecutor.getThreadNamePrefix());
        });
    }
}
