package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "缓存值预览输出模型")
@Data
@Builder
public class CacheValueVO {
    @Schema(description = "当前预览的缓存条目键")
    private String key;
    @Schema(description = "Redis 或本地缓存值的数据类型")
    private String type;
    @Schema(description = "缓存值预览是否因条目数或字节数限制被截断")
    private boolean truncated;
    @Schema(description = "缓存值条目列表")
    private List<CacheValueItemVO> items;
}
