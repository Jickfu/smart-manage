package sm.infrastructure.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 二次开发迁移配置；版本依赖属于业务发行包契约，不能从 SQL 内容猜测。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "smart-manage.infrastructure.migration.business")
public class BusinessMigrationProperties {
    private boolean enabled;
    private String location = "classpath:db/business";
    private String minimumPlatformVersion = "2";
}
