package sm.domain.sys.monitor.cache.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 缓存条目批量删除命令。
 */
@Schema(description = "缓存条目删除输入表单")
@Data
public class CacheEntryDeleteForm {
    @Schema(description = "待删除的受控缓存条目键列表")
    @Valid
    @NotEmpty
    @Size(max = 100)
    private List<CacheEntryKeyForm> entries;
}
