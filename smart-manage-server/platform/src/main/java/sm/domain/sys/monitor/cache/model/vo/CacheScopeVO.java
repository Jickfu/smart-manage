package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 缓存所属领域与应用树节点。
 */
@Schema(description = "缓存范围输出模型")
@Data
@Builder
public class CacheScopeVO {
    @Schema(description = "缓存目录节点类型：APPLICATION、CACHE、INFRASTRUCTURE")
    private String type;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "受控缓存资源标识")
    private String resourceKey;
    @Schema(description = "子节点列表")
    private List<CacheScopeVO> children;
}
