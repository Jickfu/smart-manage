package sm.framework.config;

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceWrapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** 主 Druid 数据源配置，显式隔离 Quartz 数据源并保留 Druid Starter 的完整初始化生命周期。 */
@Configuration(proxyBeanMethods = false)
public class DruidDataSourceConfig {
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.druid")
    DruidDataSourceWrapper dataSource() {
        return new DruidDataSourceWrapper();
    }
}
