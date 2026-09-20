package sm.domain.sys.monitor.cache.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "应用缓存清理")
public class CacheClearForm {

    @NotBlank(message = "缓存名称不能为空")
    @Schema(description = "受控缓存名称")
    private String cacheName;
}
