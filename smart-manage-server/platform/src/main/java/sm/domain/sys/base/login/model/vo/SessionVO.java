package sm.domain.sys.base.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import sm.domain.sys.base.user.model.vo.UserInfoVO;

/**
 * 当前浏览器登录会话初始化数据。
 */
@Schema(description = "当前会话输出模型")
@Data
@AllArgsConstructor
public class SessionVO {
    @Schema(description = "当前登录用户信息")
    private UserInfoVO user;
    @Schema(description = "当前会话的 CSRF 校验令牌")
    private String csrfToken;
}
