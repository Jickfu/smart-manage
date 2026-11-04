package sm.domain.sys.message.email.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;
import java.time.LocalDateTime;

/**
 * 邮件任务持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_email_task")
public class EmailTaskEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    /**
     * 邮件场景的稳定标识
     */
    private String sceneKey;
    /**
     * 业务幂等键
     */
    private String idempotencyKey;
    /**
     * 重新发送时关联的原始邮件任务 ID
     */
    private Long sourceTaskId;
    /**
     * 发信账号 ID
     */
    private Long accountId;
    /**
     * 发信账号编码快照
     */
    private String accountNumber;
    /**
     * 发件人邮箱地址
     */
    private String fromAddress;
    /**
     * 发件人显示名称
     */
    private String fromName;
    /**
     * JSON 格式收件人地址快照
     */
    private String toAddresses;
    /**
     * 抄送邮箱地址快照
     */
    private String ccAddresses;
    /**
     * 密送邮箱地址快照
     */
    private String bccAddresses;
    /**
     * 主题
     */
    private String subject;
    /**
     * HTML 格式邮件正文
     */
    private String htmlBody;
    /**
     * 纯文本邮件正文
     */
    private String textBody;
    /**
     * 是否为敏感正文
     */
    private Boolean sensitiveContent;
    /**
     * 敏感HTML正文密文
     */
    private String htmlBodyCipher;
    /**
     * 敏感纯文本正文密文
     */
    private String textBodyCipher;
    /**
     * 邮件任务状态：PENDING、SENDING、SUCCESS、RETRY_WAIT、FAILED、UNKNOWN、CANCELLED
     */
    private String status;
    /**
     * 邮件投递尝试次数
     */
    private Integer attemptCount;
    /**
     * 最大投递尝试次数
     */
    private Integer maxAttempts;
    /**
     * 下次尝试时间
     */
    private LocalDateTime nextAttemptTime;
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
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 乐观锁版本号
     */
    @Version private Integer version;
}
