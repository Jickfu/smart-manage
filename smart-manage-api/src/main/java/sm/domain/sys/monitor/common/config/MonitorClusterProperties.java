package sm.domain.sys.monitor.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 多实例监控注册和节点间定向调用配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-manage.monitor.cluster")
public class MonitorClusterProperties {
    private String internalBaseUrl;
    private boolean requireHttps;
    private long heartbeatIntervalMs = 10000;
    private long instanceTtlMs = 30000;
    private long requestTimeoutMs = 10000;
}
