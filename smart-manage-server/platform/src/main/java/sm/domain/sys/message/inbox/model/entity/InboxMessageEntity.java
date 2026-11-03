package sm.domain.sys.message.inbox.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 站内消息主体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_inbox_message")
public class InboxMessageEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 站内消息场景的稳定标识
     */
    private String sceneKey;
    /**
     * 业务幂等键
     */
    private String idempotencyKey;
    /**
     * 标题
     */
    private String title;
    /**
     * 消息正文
     */
    private String content;
    /**
     * 消息级别：NORMAL 普通、IMPORTANT 重要、URGENT 紧急
     */
    private String level;
    /**
     * 消息发布状态：DRAFT、PENDING、PUBLISHING、PUBLISHED、FAILED
     */
    private String status;
    /**
     * 发布人ID
     */
    private Long senderUserId;
    /**
     * 发布人名称快照
     */
    private String senderName;
    /**
     * 收件范围：ALL_ENABLED_USERS 全部启用用户、USERS 指定用户
     */
    private String audienceType;
    /**
     * 收件人数
     */
    private Long recipientCount;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 关联业务资源ID
     */
    private String resourceId;
    /**
     * 关联业务动作编码
     */
    private String actionCode;
    /**
     * 关联业务动作参数
     */
    private String actionPayload;
    /**
     * 消息发布尝试次数
     */
    private Integer attemptCount;
    /**
     * 任务领取时间
     */
    private LocalDateTime claimedTime;
    /**
     * 错误说明
     */
    private String errorMessage;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}

