package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "调度统计摘要输出模型")
@Data
@Builder
public class SchedulerSummaryVO {
    @Schema(description = "定时任务总数")
    private long totalJobs;
    @Schema(description = "已启用的定时任务数")
    private long enabledJobs;
    @Schema(description = "已暂停的定时任务数")
    private long pausedJobs;
    @Schema(description = "正在执行的任务实例数")
    private long runningExecutions;
    @Schema(description = "今日任务执行次数")
    private long todayExecutions;
    @Schema(description = "今日任务失败执行次数")
    private long todayFailedExecutions;
    @Schema(description = "按日期统计的任务执行趋势")
    private List<SchedulerTrendVO> trends;
}
