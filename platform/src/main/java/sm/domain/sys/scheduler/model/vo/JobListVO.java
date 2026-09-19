package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务列表项
 *
 * @author Chekfu
 */
@Data
@Schema(description = "任务列表项")
public class JobListVO {

    private Long id;

    private String number;

    private String jobName;

    private Long appId;
    private String appName;
    private Long domainId;
    private String domainName;

    private String jobClassName;

    private String mutexKey;

    private String cronExpression;

    private String status;

    private String description;

    private Boolean isSystem;

    private Integer version;

    /** 上次执行时间 */
    private LocalDateTime lastExecuteTime;

    /** 上次执行结果 */
    private String lastExecuteStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
