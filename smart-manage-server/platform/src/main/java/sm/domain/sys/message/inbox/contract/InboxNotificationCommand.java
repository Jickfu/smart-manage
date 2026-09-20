package sm.domain.sys.message.inbox.contract;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统内部站内通知命令。调用方必须提供稳定幂等键；resource/action 仅表达业务引用，不承载通用路由。
 */
public record InboxNotificationCommand(
        String sceneKey,
        String idempotencyKey,
        List<Long> recipientUserIds,
        String title,
        String content,
        String level,
        LocalDateTime expireTime,
        String resourceType,
        String resourceId,
        String actionCode,
        String actionPayload) {
}
