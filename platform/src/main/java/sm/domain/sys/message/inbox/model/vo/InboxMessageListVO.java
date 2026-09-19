package sm.domain.sys.message.inbox.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端消息列表。 */
@Data
public class InboxMessageListVO {
    private Long id;
    private Integer version;
    private String title;
    private String level;
    private String status;
    private String senderName;
    private Long recipientCount;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private String errorMessage;
}

