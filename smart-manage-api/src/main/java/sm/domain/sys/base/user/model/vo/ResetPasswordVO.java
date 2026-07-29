package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "重置密码结果")
public record ResetPasswordVO(@Schema(description = "仅本次返回的随机密码") String password) {
}
