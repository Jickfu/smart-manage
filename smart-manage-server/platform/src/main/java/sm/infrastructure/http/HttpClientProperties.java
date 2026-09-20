package sm.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 后端出站 HTTP 请求的全局默认配置。 */
@ConfigurationProperties(prefix = "smart-manage.infrastructure.http")
public class HttpClientProperties {
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration requestTimeout = Duration.ofSeconds(10);

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = requirePositive(connectTimeout, "connect-timeout");
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requirePositive(requestTimeout, "request-timeout");
    }

    private Duration requirePositive(Duration value, String propertyName) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(propertyName + " 必须大于 0");
        }
        return value;
    }
}
