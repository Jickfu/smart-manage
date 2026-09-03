package sm.domain.sys.base.user.model;

import java.util.Locale;
import java.util.Objects;

/** 验证码签发/消费时的不可变凭据事实，必须原样带入最终条件写入。 */
public record UserCredentialSnapshot(Long userId, String email, Long generation) {
    public UserCredentialSnapshot {
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(generation, "凭据代际不能为空");
        if (generation < 0) throw new IllegalArgumentException("凭据代际无效");
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
