package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_email_task")
public class EmailTaskEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private String sceneKey;
    private String idempotencyKey;
    private Long sourceTaskId;
    private Long accountId;
    private String accountNumber;
    private String fromAddress;
    private String fromName;
    private String toAddresses;
    private String ccAddresses;
    private String bccAddresses;
    private String subject;
    private String htmlBody;
    private String textBody;
    private Boolean sensitiveContent;
    private String htmlBodyCipher;
    private String textBodyCipher;
    private String status;
    private Integer attemptCount;
    private Integer maxAttempts;
    private LocalDateTime nextAttemptTime;
    private LocalDateTime startedTime;
    private LocalDateTime completedTime;
    private String errorCategory;
    private String errorMessage;
    private String traceId;
    @Version private Integer version;
}
