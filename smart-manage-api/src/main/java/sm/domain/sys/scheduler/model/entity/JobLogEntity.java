package sm.domain.sys.scheduler.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行实例（即执行日志）
 *
 * @author Chekfu
 */
@Data
@TableName("t_sys_job_log")
public class JobLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联的任务ID */
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 任务分组 */
    private String jobGroup;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 耗时（毫秒） */
    private Long durationMs;

    /** 状态：RUNNING / SUCCESS / FAILED */
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 本次执行链路追踪ID */
    private String traceId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
