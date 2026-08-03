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
    private List<ManagedCacheVO> caches;
    private LocalDateTime collectedAt;
}
