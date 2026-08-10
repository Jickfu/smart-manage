package sm.domain.sys.monitor.thread.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 线程热点或全量快照的受约束采集参数。 */
@Data
public class ThreadCollectForm {
    @Size(max = 160, message = "实例ID不能超过160个字符")
    private String instanceId;

    @Min(value = 200, message = "采样时间不能小于200毫秒")
    @Max(value = 5000, message = "采样时间不能超过5000毫秒")
    private Integer sampleMillis;

    @Min(value = 1, message = "线程数量不能小于1")
    @Max(value = 50, message = "线程数量不能超过50")
    private Integer limit;

    @Min(value = 1, message = "堆栈深度不能小于1")
    @Max(value = 256, message = "堆栈深度不能超过256")
    private Integer maxDepth;
}
