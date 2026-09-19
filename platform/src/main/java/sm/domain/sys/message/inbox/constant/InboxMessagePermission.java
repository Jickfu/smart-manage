package sm.domain.sys.message.inbox.constant;

/** 站内消息管理员权限。 */
public final class InboxMessagePermission {
    public static final String LIST = "sys:message:inbox-broadcast:listPage";
    public static final String DETAIL = "sys:message:inbox-broadcast:detail";
    public static final String SAVE = "sys:message:inbox-broadcast:save";
    public static final String PUBLISH = "sys:message:inbox-broadcast:publish";
    public static final String RETRY = "sys:message:inbox-broadcast:retry";

    private InboxMessagePermission() {
    }
}

