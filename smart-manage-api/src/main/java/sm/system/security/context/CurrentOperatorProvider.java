package sm.system.security.context;

/** 为基础设施提供最小化的当前操作人信息，避免反向依赖具体用户领域。 */
public interface CurrentOperatorProvider {

    Long getCurrentUserIdOrNull();

    String getCurrentUsernameOrDefault(String defaultUsername);
}
