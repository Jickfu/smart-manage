package sm.domain.sys.monitor.redis.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedisKeysForm {
    @Pattern(regexp = "\\d+", message = "SCAN 游标格式不正确")
    private String cursor = "0";
    @Size(max = 512, message = "Key 匹配模式不能超过 512 个字符")
    private String pattern = "*";
    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private Integer count = 30;
}
