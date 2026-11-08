package sm.domain.sys.scheduler.model.vo;

import java.time.LocalDate;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/** 数据库按日期和状态聚合的内部查询结果。 */
@Schema(description = "调度每日状态统计查询结果")
@Data
public class SchedulerDailyCountVO {
    @Schema(description = "执行开始日期")
    private LocalDate date;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "执行次数")
    private long count;
}
