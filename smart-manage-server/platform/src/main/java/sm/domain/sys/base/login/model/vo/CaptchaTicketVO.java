package sm.domain.sys.base.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 滑块校验通过后签发的短时一次性登录票据。
 */
@Schema(description = "验证码票据输出模型")
@Data
@AllArgsConstructor
public class CaptchaTicketVO {
    @Schema(description = "验证码校验通过后取得的一次性票据")
    private String captchaTicket;
}
