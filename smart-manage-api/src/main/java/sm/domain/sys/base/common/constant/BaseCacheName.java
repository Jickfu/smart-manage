package sm.domain.sys.base.common.constant;

/** 系统服务云下系统建模应用的 JetCache 缓存名称与固定逻辑 Key。 */
public final class BaseCacheName {
    public static final String USER_INFO = BaseKeyPrefix.VALUE + "user-info";
    public static final String SYS_PARAM = BaseKeyPrefix.VALUE + "sys-param";
    public static final String BASIC_DATA_OPTIONS = BaseKeyPrefix.VALUE + "basic-data-options";

    public static final String ALL_KEY = "all";

    private BaseCacheName() {
    }
}
