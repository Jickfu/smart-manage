package sm.domain.sys.monitor.loginlog.constant;

/** 认证日志事件类型。 */
public enum LoginEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    PASSWORD_CHANGE_REQUIRED,
    LOGOUT,
    SESSION_KICKED,
    SESSION_REPLACED,
    ACCOUNT_DISABLED,
    PASSWORD_RESET_TERMINATED
}
