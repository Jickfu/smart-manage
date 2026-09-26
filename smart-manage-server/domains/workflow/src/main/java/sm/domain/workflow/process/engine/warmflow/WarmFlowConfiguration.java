package sm.domain.workflow.process.engine.warmflow;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedHashSet;
import java.util.List;

/** 引擎资源随领域装配；平台不感知引擎的 Mapper 路径。 */
@Configuration(proxyBeanMethods = false)
public class WarmFlowConfiguration {
    @Bean
    WebMvcConfigurer warmFlowDesignerResources() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 定义页的稳定入口不能沿用个人开发配置中的长缓存，否则升级后可能引用已移除的旧资源。
                registry.addResourceHandler("/workflow/designer/**")
                        .addResourceLocations("classpath:/static/workflow/designer/")
                        .setCacheControl(CacheControl.noCache());
            }
        };
    }

    @Bean
    MybatisPlusPropertiesCustomizer warmFlowMapperResources() {
        return properties -> {
            var locations = new LinkedHashSet<>(List.of(properties.getMapperLocations()));
            locations.add("classpath*:warm/flow/*.xml");
            properties.setMapperLocations(locations.toArray(String[]::new));
        };
    }
}
