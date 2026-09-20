package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

/** 本地与 Redis 缓存的统一列表投影。 */
@Data
@Builder
public class CacheEntryVO {
    private String identity;
    private String storage;
    private String cacheName;
    private String cacheDisplayName;
    private String key;
    private String type;
    private Long ttl;
    private Long memoryBytes;
    private boolean valueReadable;
    private boolean currentNodeOnly;
}
