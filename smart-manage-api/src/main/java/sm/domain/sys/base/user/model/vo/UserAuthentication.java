package sm.domain.sys.base.user.model.vo;

/**
 * 用户凭据校验结果，仅用于登录服务内部编排。
 */
public record UserAuthentication(
        Long userId,
        String username,
        String name,
        boolean passwordReset,
        boolean administrator,
        Long orgId,
        Long credentialGeneration,
        String message) {

    public static UserAuthentication failed(String message) {
        return new UserAuthentication(null, null, null, false, false, null, null, message);
    }

    public boolean successful() {
        return userId != null;
    }
}
