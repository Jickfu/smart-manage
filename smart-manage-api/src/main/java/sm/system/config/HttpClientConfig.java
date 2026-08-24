package sm.system.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sm.system.http.HttpClientProperties;

import java.net.http.HttpClient;

/** 后端出站 HTTP 客户端配置。 */
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientConfig {

    @Bean
    public HttpClient outboundHttpClient(HttpClientProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                // 跨主机重定向可能泄露 Cookie 或自定义认证头，默认交由调用方显式处理。
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }
}
