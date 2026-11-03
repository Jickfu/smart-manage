package sm.domain.sys.monitor.cache.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 唯一定位一个本地或 Redis 缓存条目。
 */
@Schema(description = "缓存条目键输入表单")
@Data
public class CacheEntryKeyForm {
    @Schema(description = "缓存存储层")
    @NotBlank
    private String storage;
    @Schema(description = "受控缓存名称")
    private String cacheName;
    @Schema(description = "受控目录内的缓存条目键")
    @NotBlank
    private String key;
}
