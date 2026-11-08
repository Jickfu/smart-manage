package sm.domain.sys.monitor.cache.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

@Schema(description = "缓存值条目输出模型")
@Data
@Builder
public class CacheValueItemVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "字段值")
    private String value;
    @Schema(description = "Redis 有序集合条目的分值")
    private Double score;
    @Schema(description = "值是否以 Base64 编码返回")
    private boolean base64;
}
