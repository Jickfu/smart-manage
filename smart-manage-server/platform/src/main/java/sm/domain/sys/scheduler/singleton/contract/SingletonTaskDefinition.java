package sm.domain.sys.scheduler.singleton.contract;

/** 代码注册的稳定任务元数据；启动默认值仅用于首次初始化，不覆盖管理员期望。 */
public record SingletonTaskDefinition(String number, String name, String appNumber,
        String description, boolean controllable, boolean autoStart, int retrySeconds) {
    public SingletonTaskDefinition {
        if (number == null || !number.matches("[a-z][a-z0-9:._-]{2,119}")
                || name == null || name.isBlank() || name.length() > 120
                || appNumber == null || appNumber.isBlank() || appNumber.length() > 120
                || description == null || description.length() > 1000
                || retrySeconds < 1 || retrySeconds > 86400) {
            throw new IllegalArgumentException("单例任务定义不合法");
        }
    }
}
