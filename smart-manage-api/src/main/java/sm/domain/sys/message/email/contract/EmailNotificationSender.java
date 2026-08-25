package sm.domain.sys.message.email.contract;

/** 业务模块提交系统邮件的最小稳定边界；SMTP 投递仍由消息应用异步处理。 */
public interface EmailNotificationSender {
    Long enqueue(EmailNotificationCommand command);
}
