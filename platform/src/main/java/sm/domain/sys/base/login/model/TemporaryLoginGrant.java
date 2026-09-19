package sm.domain.sys.base.login.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Redis 中保存的一次性代登录授权，不保存明文凭证。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryLoginGrant {
    private String grantId;
    private Long issuerUserId;
    private Long targetUserId;
    private String targetUsername;
    private String reason;
    private LocalDateTime expiresAt;
}
