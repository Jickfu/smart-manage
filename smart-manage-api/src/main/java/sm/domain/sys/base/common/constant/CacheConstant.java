package sm.domain.sys.base.common.constant;

/**
 * 系统基础领域缓存名称与固定键。
 *
 * <p>共享业务状态统一使用远程缓存，保证多节点读取和失效语义一致。</p>
 */
public final class CacheConstant {

	public static final String USER_INFO = "sys:base:user-info";
	public static final String SYS_PARAM = "sys:base:sys-param";
	public static final String UI_CONFIG = "sys:base:ui-config";
	public static final String FILE_CONFIG = "sys:base:file-config";
	public static final String BASIC_DATA_OPTIONS = "sys:base:basic-data-options";

	public static final String SINGLETON_KEY = "current";
	public static final String ALL_KEY = "all";

	private CacheConstant() {
	}
}
