package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务详情
 *
 * @author Chekfu
 */
@Data
@Schema(description = "任务详情")
public class JobDetailVO {

    private Long id;

    private String number;

    private String jobName;

    private String jobGroup;

    private String jobClassName;

    private String cronExpression;

    private String jobData;

    private String mutexKey;

    private String status;

    private String description;

    private Boolean isSystem;

    private Integer version;

    /** 下次触发时间（从 Quartz Trigger 获取） */
    private LocalDateTime nextFireTime;

    /** 上次执行时间 */
    private LocalDateTime lastExecuteTime;

    /** 上次执行结果 */
    private String lastExecuteStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
