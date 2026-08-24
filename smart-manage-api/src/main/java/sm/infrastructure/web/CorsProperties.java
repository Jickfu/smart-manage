package sm.infrastructure.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 跨域访问配置。
 *
 * @param allowedOrigins 允许访问后端的前端来源表达式
 */
@ConfigurationProperties(prefix = "smart-manage.infrastructure.cors")
public record CorsProperties(List<String> allowedOrigins) {

	public CorsProperties {
		allowedOrigins = List.copyOf(allowedOrigins);
	}
}
