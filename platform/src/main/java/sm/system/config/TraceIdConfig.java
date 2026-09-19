package sm.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sm.system.interceptor.TraceIdInterceptor;

/**
 * @author Chekfu
 */
@Configuration
@RequiredArgsConstructor
public class TraceIdConfig implements WebMvcConfigurer {
	private final TraceIdInterceptor traceIdInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(traceIdInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/favicon.ico", "**/css/**", "**/js/**");
	}
}
