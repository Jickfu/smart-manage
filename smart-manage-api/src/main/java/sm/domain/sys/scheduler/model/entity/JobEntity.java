package sm.domain.sys.scheduler.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 定时任务定义
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_job")
public class JobEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 任务编码 */
    private String number;

    /** 任务名称 */
    private String jobName;

    /** 任务分组 */
    private String jobGroup;

    /** Job 实现类全限定名 */
    private String jobClassName;

    /** Cron 表达式 */
    private String cronExpression;

    /** 任务参数（JSON） */
    private String jobData;

    /** 共享资源互斥键；不同任务使用相同键时不并行执行 */
    private String mutexKey;

    /** 状态：ENABLED / PAUSED */
    private String status;

    /** 是否系统内置 */
    private Boolean isSystem;

    /** 描述 */
    private String description;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
