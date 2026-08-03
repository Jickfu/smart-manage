package sm.domain.sys.monitor.redis.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisValueItemVO {
    private String name;
    private String value;
    private Double score;
    private boolean base64;
}
