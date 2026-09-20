package sm.infrastructure.scheduler;

import com.alibaba.druid.pool.DruidDataSource;
import org.quartz.spi.JobFactory;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.quartz.autoconfigure.QuartzDataSource;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Quartz 调度器配置
 *
 * @author Chekfu
 */
@Configuration
public class QuartzConfiguration {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * Quartz 专用 DataSource，@QuartzDataSource 让 Spring Boot 自动注入到 SchedulerFactoryBean
     */
    @Bean(defaultCandidate = false)
    @QuartzDataSource
    DataSource quartzDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxActive(5);
        ds.setName("QuartzPool");
        return ds;
    }

    /**
     * SpringBeanJobFactory — 让 Quartz Job 支持 Spring 依赖注入，取代手工 DelegatingJob 桥接
     */
    @Bean
    JobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        SpringBeanJobFactory factory = new SpringBeanJobFactory();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    /**
     * 注册全局 Job 监听器（setDataSource 由 QuartzAutoConfiguration 自动完成）
     */
    @Bean
    SchedulerFactoryBeanCustomizer quartzCustomizer(List<JobListener> listeners) {
        return factoryBean -> factoryBean.setGlobalJobListeners(listeners.toArray(JobListener[]::new));
    }
}
