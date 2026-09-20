package sm.domain.sys.message.email.constant;

public final class EmailPermission {
    public static final String ACCOUNT_LIST = "sys:message:email-account:listPage";
    public static final String ACCOUNT_DETAIL = "sys:message:email-account:detail";
    public static final String ACCOUNT_SAVE = "sys:message:email-account:save";
    public static final String ACCOUNT_ENABLE = "sys:message:email-account:enable";
    public static final String ACCOUNT_DELETE = "sys:message:email-account:delete";
    public static final String ACCOUNT_TEST = "sys:message:email-account:test";
    public static final String COMPOSE_SEND = "sys:message:email-compose:send";
    public static final String RECORD_LIST = "sys:message:email-record:listPage";
    public static final String RECORD_DETAIL = "sys:message:email-record:detail";
    public static final String RECORD_RETRY = "sys:message:email-record:retry";
    public static final String RECORD_CANCEL = "sys:message:email-record:cancel";
    private EmailPermission() {}
}
