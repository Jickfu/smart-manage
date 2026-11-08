package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "调度统计趋势输出模型")
@Data
@AllArgsConstructor
public class SchedulerTrendVO {
    @Schema(description = "统计日期，使用 Asia/Shanghai 时区")
    private LocalDate date;
    @Schema(description = "当日成功执行数量")
    private long success;
    @Schema(description = "当日失败执行数量")
    private long failed;
    @Schema(description = "跳过执行数量")
    private long skipped;
}
