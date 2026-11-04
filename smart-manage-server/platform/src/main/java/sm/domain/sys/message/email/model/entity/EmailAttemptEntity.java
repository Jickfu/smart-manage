package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 邮件投递尝试持久化实体。
 */
@Data
@TableName("t_sys_email_attempt")
public class EmailAttemptEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    /**
     * 邮件任务 ID
     */
    private Long taskId;
    /**
     * 本次投递尝试序号
     */
    private Integer attemptNo;
    /**
     * 投递尝试状态：SENDING、SUCCESS、FAILED、UNKNOWN
     */
    private String status;
    /**
     * 投递开始时间
     */
    private LocalDateTime startedTime;
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    /**
     * 投递错误分类
     */
    private String errorCategory;
    /**
     * 错误说明
     */
    private String errorMessage;
    /**
     * 应用实例唯一标识
     */
    private String instanceId;
    /**
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
