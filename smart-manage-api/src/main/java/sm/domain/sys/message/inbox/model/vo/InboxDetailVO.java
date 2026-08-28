package sm.domain.sys.message.inbox.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 当前用户消息详情。 */
@Data
public class InboxDetailVO {
    private Long messageId;
    private String title;
    private String content;
    private String level;
    private String senderName;
    private Boolean readStatus;
    private LocalDateTime readTime;
    /** 数据库微秒精度的稳定收件键。 */
    private String receivedTime;
    private LocalDateTime expireTime;
    private String resourceType;
    private String resourceId;
    private String actionCode;
    private String actionPayload;
}
