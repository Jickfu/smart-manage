package sm.system.security;

/** 认证请求的持久化安全状态校验；实现必须绕过共享缓存，查询失败时拒绝请求。 */
public interface AuthenticatedSessionStateVerifier {
    void verify(Long userId, long credentialGeneration);
}
