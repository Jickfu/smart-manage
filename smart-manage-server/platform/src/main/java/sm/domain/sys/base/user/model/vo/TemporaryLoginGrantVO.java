package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仅在生成成功时返回一次的代登录凭证。
 */
@Schema(description = "临时代登录授权输出模型")
@Data
@AllArgsConstructor
public class TemporaryLoginGrantVO {
    @Schema(description = "仅本次返回的临时代登录凭证")
    private String credential;
    @Schema(description = "临时代登录凭证失效时间")
    private LocalDateTime expiresAt;
}
