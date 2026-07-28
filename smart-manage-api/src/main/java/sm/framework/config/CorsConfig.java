package sm.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 * 跨域配置
 *
 * @author Chekfu
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

	private final CorsProperties corsProperties;

	@Value("${sa-token.token-name:smtoken}")
	private String tokenName;

	public CorsConfig(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		log.info("初始化 CORS 配置，允许的源：{}", corsProperties.allowedOrigins());
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);

		// 设置允许的源
		for (String origin : corsProperties.allowedOrigins()) {
			config.addAllowedOriginPattern(origin);
		}

		// 设置允许的HTTP方法和头
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.addExposedHeader(tokenName);

		// 注册CORS配置
		source.registerCorsConfiguration("/**", config);

		// 创建并配置Filter，要在sa-token之前注册，否者不生效
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
}
