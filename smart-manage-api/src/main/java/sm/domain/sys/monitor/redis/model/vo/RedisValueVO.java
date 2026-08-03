package sm.domain.sys.monitor.redis.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RedisValueVO {
    private String key;
    private String type;
    private boolean truncated;
    private List<RedisValueItemVO> items;
}
