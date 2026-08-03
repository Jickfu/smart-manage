package sm.domain.sys.monitor.redis.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedisKeyForm {
    @NotBlank(message = "Redis Key 不能为空")
    @Size(max = 1024, message = "Redis Key 不能超过 1024 个字符")
    private String key;
}
