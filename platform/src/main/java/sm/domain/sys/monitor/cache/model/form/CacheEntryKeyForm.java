package sm.domain.sys.monitor.cache.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 唯一定位一个本地或 Redis 缓存条目。 */
@Data
public class CacheEntryKeyForm {
    @NotBlank
    private String storage;
    private String cacheName;
    @NotBlank
    private String key;
}
