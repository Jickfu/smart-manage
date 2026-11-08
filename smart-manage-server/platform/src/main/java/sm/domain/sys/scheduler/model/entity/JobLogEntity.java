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

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的任务ID
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 执行时的业务归属快照；任务改属或删除不改变历史
     */
    private Long domainId;
    /**
     * 执行时所属领域名称快照
     */
    private String domainName;
    /**
     * 所属应用ID
     */
    private Long appId;
    /**
     * 执行时所属应用名称快照
     */
    private String appName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 任务执行状态：RUNNING 执行中、SUCCESS 成功、FAILED 失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 本次执行链路追踪ID
     */
    private String traceId;

    /**
     * 实际执行任务的应用实例标识（Quartz instanceId）
     */
    private String instanceId;

    /**
     * Quartz 本次触发实例 ID
     */
    private String fireInstanceId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
