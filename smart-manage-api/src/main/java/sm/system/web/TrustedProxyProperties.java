package sm.system.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 只有这些网段内的直接上游才允许提供客户端转发地址。 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-manage.framework.trusted-proxy")
public class TrustedProxyProperties {
    private List<String> cidrs = new ArrayList<>();
}
