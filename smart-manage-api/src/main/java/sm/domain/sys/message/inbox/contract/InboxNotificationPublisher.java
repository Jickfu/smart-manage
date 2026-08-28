package sm.domain.sys.message.inbox.contract;

/** 面向工作流等未来生产者的稳定站内通知发布边界。 */
public interface InboxNotificationPublisher {
    Long publish(InboxNotificationCommand command);
}
