package sm.domain.sys.base.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 仅在生成成功时返回一次的代登录凭证。 */
@Data
@AllArgsConstructor
public class TemporaryLoginGrantVO {
    private String credential;
    private LocalDateTime expiresAt;
}
