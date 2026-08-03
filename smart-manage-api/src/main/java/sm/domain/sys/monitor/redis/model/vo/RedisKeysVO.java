package sm.domain.sys.monitor.redis.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RedisKeysVO {
    private String nextCursor;
    private boolean finished;
    private List<RedisKeyVO> records;
}
