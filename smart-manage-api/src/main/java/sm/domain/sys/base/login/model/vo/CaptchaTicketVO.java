package sm.domain.sys.base.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 滑块校验通过后签发的短时一次性登录票据。 */
@Data
@AllArgsConstructor
public class CaptchaTicketVO {
    private String captchaTicket;
}
