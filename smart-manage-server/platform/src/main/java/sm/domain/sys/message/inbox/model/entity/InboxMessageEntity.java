package sm.domain.sys.message.inbox.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDateTime;

/** 站内消息主体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_inbox_message")
public class InboxMessageEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String sceneKey;
    private String idempotencyKey;
    private String title;
    private String content;
    private String level;
    private String status;
    private Long senderUserId;
    private String senderName;
    private String audienceType;
    private Long recipientCount;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private String resourceType;
    private String resourceId;
    private String actionCode;
    private String actionPayload;
    private Integer attemptCount;
    private LocalDateTime claimedTime;
    private String errorMessage;
    @Version
    private Integer version;
}

