package sm.domain.sys.base.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import sm.domain.sys.base.user.model.vo.UserInfoVO;

/** 当前浏览器登录会话初始化数据。 */
@Data
@AllArgsConstructor
public class SessionVO {
    private UserInfoVO user;
    private String csrfToken;
}
