package sm.domain.sys.base.common.constant;

/** 系统服务领域下系统管理应用直接通过 RedisTemplate 操作的物理 Key。 */
public final class BaseRedisKey {
    public static final String CAPTCHA = BaseKeyPrefix.VALUE + "captcha:";
    public static final String PASSWORD_CHANGE_TICKET = BaseKeyPrefix.VALUE + "password-change:";
    public static final String TEMPORARY_LOGIN_GRANT = BaseKeyPrefix.VALUE + "temporary-login:";

    private BaseRedisKey() {
    }
}
