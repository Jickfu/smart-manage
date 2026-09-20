package sm.infrastructure.scheduler;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.Test;
import org.quartz.JobListener;
import org.quartz.spi.JobFactory;
import org.springframework.boot.quartz.autoconfigure.QuartzDataSource;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

class QuartzConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(QuartzConfiguration.class)
            .withPropertyValues(
                    "spring.datasource.url=jdbc:postgresql://localhost:5432/smart_manage",
                    "spring.datasource.username=smart_manage",
                    "spring.datasource.password=test-password");

    @Test
    void registersStableQuartzBeansAndDedicatedDataSourceSettings() {
        contextRunner.run(context -> {
            DataSource dataSource = context.getBean("quartzDataSource", DataSource.class);
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;

            assertEquals("jdbc:postgresql://localhost:5432/smart_manage", druidDataSource.getUrl());
            assertEquals("smart_manage", druidDataSource.getUsername());
            assertEquals("test-password", druidDataSource.getPassword());
            assertEquals(5, druidDataSource.getMaxActive());
            assertEquals("QuartzPool", druidDataSource.getName());
            assertNotNull(context.getBean("springBeanJobFactory", JobFactory.class));
            assertNotNull(context.getBean("quartzCustomizer", SchedulerFactoryBeanCustomizer.class));

            druidDataSource.close();
        });
    }

    @Test
    void keepsQuartzDataSourceQualifierAndNonDefaultCandidate() throws NoSuchMethodException {
        var beanMethod = QuartzConfiguration.class.getDeclaredMethod("quartzDataSource");
        assertNotNull(beanMethod.getAnnotation(QuartzDataSource.class));
        assertFalse(beanMethod.getAnnotation(Bean.class).defaultCandidate());
    }

    @Test
    void registersAllGlobalJobListeners() {
        JobListener listener = mock(JobListener.class);
        SchedulerFactoryBean factory = mock(SchedulerFactoryBean.class);

        new QuartzConfiguration().quartzCustomizer(List.of(listener)).customize(factory);

        verify(factory).setGlobalJobListeners(listener);
    }
}
