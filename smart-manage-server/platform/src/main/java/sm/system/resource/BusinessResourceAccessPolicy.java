package sm.system.resource;

/**
 * 业务资源授权策略。具体业务模块负责校验资源存在性、当前状态和用户权限。
 */
public interface BusinessResourceAccessPolicy {
    void requireAllowed(String resourceId, BusinessResourceAction action);
}
