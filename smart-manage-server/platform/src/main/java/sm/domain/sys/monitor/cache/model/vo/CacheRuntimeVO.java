package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "缓存运行快照输出模型")
@Data
@Builder
public class CacheRuntimeVO {
    @Schema(description = "Redis 当前是否可连接并读取运行信息")
    private boolean available;
    @Schema(description = "Redis 服务版本")
    private String version;
    @Schema(description = "Redis 运行时长，单位秒")
    private long uptimeSeconds;
    @Schema(description = "Redis 已使用内存，单位字节")
    private long usedMemoryBytes;
    @Schema(description = "Redis 已使用内存的可读文本")
    private String usedMemoryDisplay;
    @Schema(description = "Redis maxmemory 配置，单位字节，0 表示未设置内存上限")
    private long maxMemoryBytes;
    @Schema(description = "Redis 当前客户端连接数")
    private int connectedClients;
    @Schema(description = "当前 Redis 数据库键数量")
    private long dbSize;
    @Schema(description = "Redis 累计命中次数")
    private long keyspaceHits;
    @Schema(description = "Redis 累计未命中次数")
    private long keyspaceMisses;
    @Schema(description = "Redis 累计命中率，原始比值 0～1；尚无命中或未命中统计时为 null")
    private Double hitRate;
    @Schema(description = "Redis 数据库编号")
    private int database;
    @Schema(description = "快照采集时间")
    private LocalDateTime collectedAt;
}
