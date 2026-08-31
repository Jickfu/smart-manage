package sm.domain.sys.base.user.contract;

/** 用户领域向其他业务领域发布的最小只读引用投影。 */
public record UserReference(Long id, String number, String name, String username, String email,
                            boolean enabled) {
}
