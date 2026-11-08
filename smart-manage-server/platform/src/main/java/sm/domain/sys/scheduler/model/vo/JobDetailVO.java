package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

import java.time.LocalDateTime;

/**
 * 任务详情
 *
 * @author Chekfu
 */
@Data
@Schema(description = "任务详情")
public class JobDetailVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "业务编码")
    private String number;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "所属应用信息")
    private ReferenceVO app;

    @Schema(description = "执行类名")
    private String jobClassName;

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "任务参数")
    private String jobData;

    @Schema(description = "共享资源互斥键；不同任务使用相同键时，同一时刻只执行一个")
    private String mutexKey;

    @Schema(description = "任务状态：ENABLED 启用、PAUSED 暂停")
    private String status;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "是否系统内置")
    private Boolean isSystem;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 下次触发时间（从 Quartz Trigger 获取）
     */
    @Schema(description = "下次计划触发时间")
    private LocalDateTime nextFireTime;

    /**
     * 上次执行时间
     */
    @Schema(description = "最近一次执行时间")
    private LocalDateTime lastExecuteTime;

    /**
     * 上次执行结果
     */
    @Schema(description = "最近一次任务执行状态")
    private String lastExecuteStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
