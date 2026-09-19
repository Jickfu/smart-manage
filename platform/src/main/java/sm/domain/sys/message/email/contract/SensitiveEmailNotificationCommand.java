package sm.domain.sys.message.email.contract;

import java.util.List;

/** 验证码等不得进入管理端详情或长期保留的敏感系统邮件。 */
public record SensitiveEmailNotificationCommand(
        String sceneKey,
        String idempotencyKey,
        List<String> recipientAddresses,
        String subject,
        String htmlBody,
        String textBody) {
}
