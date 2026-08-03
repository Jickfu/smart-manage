package sm.domain.sys.monitor.redis.constant;

/** Redis 高风险运维权限。 */
public final class RedisPermission {
    public static final String LIST = "sys:monitor:redis:listPage";
    public static final String VALUE = "sys:monitor:redis:value";
    public static final String DELETE = "sys:monitor:redis:delete";

    private RedisPermission() {
    }
}
