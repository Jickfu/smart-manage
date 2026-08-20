package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 缓存所属领域与应用树节点。 */
@Data
@Builder
public class CacheScopeVO {
    private String type;
    private String name;
    private String resourceKey;
    private List<CacheScopeVO> children;
}
