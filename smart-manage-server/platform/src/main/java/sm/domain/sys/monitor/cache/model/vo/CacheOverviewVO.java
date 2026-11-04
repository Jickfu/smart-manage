package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "应用缓存概览")
public class CacheOverviewVO {
    @Schema(description = "应用实例唯一标识")
    private String instanceId;
    @Schema(description = "当前节点已登记的应用缓存统计")
    private List<ManagedCacheVO> caches;
    @Schema(description = "快照采集时间")
    private LocalDateTime collectedAt;
}
