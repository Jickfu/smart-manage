package sm.domain.sys.monitor.redis.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisKeyVO {
    private String key;
    private String type;
    private long ttl;
    private Long memoryBytes;
    private boolean valueReadable;
}
