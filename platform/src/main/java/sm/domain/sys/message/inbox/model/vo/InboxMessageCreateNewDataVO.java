package sm.domain.sys.message.inbox.model.vo;

import java.time.LocalDateTime;

/** 管理员新增消息默认值。 */
public record InboxMessageCreateNewDataVO(String level, LocalDateTime expireTime) {
}

