package sm.domain.sys.base.login.constant;

/** 登录保护系统内置参数编码。参数值和允许范围由系统参数描述统一维护。 */
public final class LoginProtectionParam {
    public static final String CAPTCHA_CHALLENGE_EXPIRE_SECONDS = "LOGIN_CAPTCHA_CHALLENGE_EXPIRE_SECONDS";
    public static final String CAPTCHA_TICKET_EXPIRE_SECONDS = "LOGIN_CAPTCHA_TICKET_EXPIRE_SECONDS";
    public static final String CAPTCHA_MIN_INTERVAL_MILLIS = "LOGIN_CAPTCHA_MIN_INTERVAL_MILLIS";
    public static final String CAPTCHA_IP_MAX_PER_MINUTE = "LOGIN_CAPTCHA_IP_MAX_PER_MINUTE";
    public static final String FAILURE_WINDOW_MINUTES = "LOGIN_FAILURE_WINDOW_MINUTES";
    public static final String ACCOUNT_MAX_FAILURES = "LOGIN_ACCOUNT_MAX_FAILURES";
    public static final String ACCOUNT_BLOCK_SECONDS = "LOGIN_ACCOUNT_BLOCK_SECONDS";
    public static final String IP_MAX_FAILURES = "LOGIN_IP_MAX_FAILURES";
    public static final String IP_BLOCK_MINUTES = "LOGIN_IP_BLOCK_MINUTES";
    public static final String ACCOUNT_IP_MAX_FAILURES = "LOGIN_ACCOUNT_IP_MAX_FAILURES";
    public static final String ACCOUNT_IP_BLOCK_MINUTES = "LOGIN_ACCOUNT_IP_BLOCK_MINUTES";

    private LoginProtectionParam() {
    }
}
