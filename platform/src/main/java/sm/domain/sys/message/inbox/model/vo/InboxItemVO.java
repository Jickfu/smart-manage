package sm.domain.sys.message.inbox.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 当前用户消息时间线条目。 */
@Data
public class InboxItemVO {
    private Long messageId;
    private String title;
    private String summary;
    private String level;
    private String senderName;
    private Boolean readStatus;
    private LocalDateTime readTime;
    /** 数据库微秒精度的稳定收件键，不能经过全局秒级时间序列化。 */
    private String receivedTime;
    private LocalDateTime expireTime;
}
