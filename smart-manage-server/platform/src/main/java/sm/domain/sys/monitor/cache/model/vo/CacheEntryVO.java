package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/**
 * 本地与 Redis 缓存的统一列表投影。
 */
@Schema(description = "缓存明细输出模型")
@Data
@Builder
public class CacheEntryVO {
    @Schema(description = "缓存条目的稳定身份标识")
    private String identity;
    @Schema(description = "缓存存储层")
    private String storage;
    @Schema(description = "受控缓存名称")
    private String cacheName;
    @Schema(description = "缓存展示名称")
    private String cacheDisplayName;
    @Schema(description = "缓存条目键")
    private String key;
    @Schema(description = "缓存值的数据类型")
    private String type;
    @Schema(description = "Redis 剩余有效期，单位秒；-1 表示永不过期，-2 表示键不存在")
    private Long ttl;
    @Schema(description = "缓存条目占用内存，单位字节")
    private Long memoryBytes;
    @Schema(description = "是否允许预览该缓存条目的值")
    private boolean valueReadable;
    @Schema(description = "是否仅代表当前应用节点的数据")
    private boolean currentNodeOnly;
}
