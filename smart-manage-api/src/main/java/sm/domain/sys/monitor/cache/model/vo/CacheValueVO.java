package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CacheValueVO {
    private String key;
    private String type;
    private boolean truncated;
    private List<CacheValueItemVO> items;
}
