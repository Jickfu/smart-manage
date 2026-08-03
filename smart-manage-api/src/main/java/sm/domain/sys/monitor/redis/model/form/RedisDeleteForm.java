package sm.domain.sys.monitor.redis.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RedisDeleteForm {
    @NotEmpty(message = "Redis Key 不能为空")
    @Size(max = 100, message = "单次最多删除 100 个 Key")
    private List<@NotBlank(message = "Redis Key 不能为空") String> keys;
}
