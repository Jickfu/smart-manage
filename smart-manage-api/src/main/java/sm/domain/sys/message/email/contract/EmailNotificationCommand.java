package sm.domain.sys.message.email.contract;

import java.util.List;

public record EmailNotificationCommand(
        String sceneKey,
        String idempotencyKey,
        List<Long> recipientUserIds,
        String subject,
        String htmlBody,
        String textBody) {
}
