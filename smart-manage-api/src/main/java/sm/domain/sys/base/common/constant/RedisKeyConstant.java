package sm.domain.sys.base.common.constant;

/**
 * @author Chekfu
 */
public class RedisKeyConstant {
	// Redis Key 前缀
	public static final String YUN = "sys:";
	public static final String BASE = "base:";
	public static final String CAPTCHA = YUN + BASE + "captcha:";
	public static final String PASSWORD_CHANGE_TICKET = YUN + BASE + "password-change:";

}
