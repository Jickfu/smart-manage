package sm.domain.sys.scheduler.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SchedulerSummaryVO {
    private long totalJobs;
    private long enabledJobs;
    private long pausedJobs;
    private long runningExecutions;
    private long todayExecutions;
    private long todayFailedExecutions;
    private List<SchedulerTrendVO> trends;
}
