package sm.domain.sys.scheduler.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SchedulerTrendVO {
    private LocalDate date;
    private long success;
    private long failed;
    private long skipped;
}
