package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_sys_email_attempt")
public class EmailAttemptEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long taskId;
    private Integer attemptNo;
    private String status;
    private LocalDateTime startedTime;
    private LocalDateTime completedTime;
    private String errorCategory;
    private String errorMessage;
    private String instanceId;
    private String traceId;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
