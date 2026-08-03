package sm.domain.sys.base.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 组织相关基础配置。 */
@Data
@Configuration
@ConfigurationProperties(prefix = "smart-manage.org")
public class OrgConfig {
	private Long defaultId = 1L;
}
