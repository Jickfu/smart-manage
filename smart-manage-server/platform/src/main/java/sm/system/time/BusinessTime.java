package sm.system.time;

import java.time.ZoneId;

/** 平台无偏移日期时间统一使用北京时间。 */
public final class BusinessTime {
    public static final String ZONE_NAME = "Asia/Shanghai";
    public static final ZoneId ZONE = ZoneId.of(ZONE_NAME);

    private BusinessTime() {
    }
}
