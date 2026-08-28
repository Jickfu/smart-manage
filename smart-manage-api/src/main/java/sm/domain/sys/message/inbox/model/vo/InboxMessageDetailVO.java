package sm.domain.sys.message.inbox.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端消息详情。 */
@Data
public class InboxMessageDetailVO {
    private Long id;
    private Integer version;
    private String title;
    private String content;
    private String level;
    private String status;
    private String senderName;
    private Long recipientCount;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private String errorMessage;
}

