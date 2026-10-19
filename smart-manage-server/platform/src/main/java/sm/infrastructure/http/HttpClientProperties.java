package sm.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 后端出站 HTTP 请求的全局默认配置。 */
@ConfigurationProperties(prefix = "smart-manage.infrastructure.http")
public class HttpClientProperties {
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration requestTimeout = Duration.ofSeconds(10);
    private long maxResponseBytes = 8L * 1024 * 1024;

    public long getMaxResponseBytes() {
        return maxResponseBytes;
    }

    public void setMaxResponseBytes(long maxResponseBytes) {
        if (maxResponseBytes < 1 || maxResponseBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("max-response-bytes 必须在 1 到 2147483647 之间");
        }
        this.maxResponseBytes = maxResponseBytes;
    }

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
