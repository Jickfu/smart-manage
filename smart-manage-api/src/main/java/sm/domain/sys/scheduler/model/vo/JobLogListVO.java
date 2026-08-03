package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行实例列表项
 *
 * @author Chekfu
 */
@Data
@Schema(description = "执行实例列表项")
public class JobLogListVO {

    private Long id;

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String status;

    private String errorMessage;

    private String traceId;

    private LocalDateTime createTime;
}
