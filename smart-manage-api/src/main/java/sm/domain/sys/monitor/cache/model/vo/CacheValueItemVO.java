package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheValueItemVO {
    private String name;
    private String value;
    private Double score;
    private boolean base64;
}
