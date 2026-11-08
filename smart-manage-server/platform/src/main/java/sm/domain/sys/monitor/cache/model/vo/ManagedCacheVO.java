package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "受控应用缓存")
public class ManagedCacheVO {

    @Schema(description = "名称")
    private String name;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "JetCache 缓存类型")
    private String type;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "缓存有效期，单位秒")
    private long expireSeconds;
    @Schema(description = "缓存估算条目数")
    private Long estimatedSize;
    @Schema(description = "当前缓存统计数据是否可用")
    private boolean statisticsAvailable;
    @Schema(description = "是否仅代表当前应用节点的数据")
    private boolean currentNodeOnly;
    @Schema(description = "当前节点缓存状态：NOT_CREATED、UNAVAILABLE、IDLE、ACTIVE；统计值仅在 statisticsAvailable 为 true 时有效")
    private String state;
    @Schema(description = "当前缓存统计周期开始时间")
    private java.time.LocalDateTime statStartedAt;
    @Schema(description = "当前缓存统计周期结束时间")
    private java.time.LocalDateTime statEndedAt;
    @Schema(description = "当前统计周期缓存读取次数")
    private long getCount;
    @Schema(description = "当前统计周期缓存命中次数")
    private long hitCount;
    @Schema(description = "当前统计周期缓存未命中次数")
    private long missCount;
    @Schema(description = "当前统计周期缓存访问失败次数")
    private long failCount;
    @Schema(description = "缓存命中率，原始比值 0～1")
    private double hitRate;
    @Schema(description = "当前统计周期缓存每秒查询次数")
    private double qps;
    @Schema(description = "平均缓存读取耗时，单位毫秒")
    private double averageGetTime;
}
