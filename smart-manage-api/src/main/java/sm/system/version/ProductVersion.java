package sm.system.version;

/** 当前后端产物的版本快照；null 表示构建环境未提供对应信息。 */
public record ProductVersion(String version) {
}
