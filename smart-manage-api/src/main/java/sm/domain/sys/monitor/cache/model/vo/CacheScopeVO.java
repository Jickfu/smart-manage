package sm.domain.sys.monitor.cache.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 缓存所属云与应用树节点。 */
@Data
@Builder
public class CacheScopeVO {
    private String type;
    private String name;
    private String cloudNumber;
    private String appNumber;
    private List<CacheScopeVO> children;
}
