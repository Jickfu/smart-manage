package sm.domain.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.concurrent.atomic.AtomicInteger;

/** 脚本网关 Bean Validation 专用测试 Service。 */
public class ValidationDemoService {
    private final AtomicInteger invocationCount = new AtomicInteger();

    public String validate(@NotNull(message = "表单不能为空") ValidationForm form) {
        invocationCount.incrementAndGet();
        return form.name();
    }

    public int invocationCount() {
        return invocationCount.get();
    }

    public record ValidationForm(
            @NotNull(message = "ID不能为空") Long id,
            @NotBlank(message = "名称不能为空")
            @Size(max = 8, message = "名称不能超过8个字符") String name,
            @Min(value = 1, message = "数量不能小于1")
            @Max(value = 10, message = "数量不能大于10") Integer quantity,
            @Valid @NotNull(message = "嵌套参数不能为空") NestedForm nested) {
    }

    public record NestedForm(@NotBlank(message = "嵌套编码不能为空") String code) {
    }
}
