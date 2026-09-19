package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "受控应用缓存")
public class ManagedCacheVO {

    private String name;
    private String displayName;
    private String type;
    private String description;
    private long expireSeconds;
    private Long estimatedSize;
    private boolean statisticsAvailable;
    private boolean currentNodeOnly;
    private String state;
    private java.time.LocalDateTime statStartedAt;
    private java.time.LocalDateTime statEndedAt;
    private long getCount;
    private long hitCount;
    private long missCount;
    private long failCount;
    private double hitRate;
    private double qps;
    private double averageGetTime;
}
