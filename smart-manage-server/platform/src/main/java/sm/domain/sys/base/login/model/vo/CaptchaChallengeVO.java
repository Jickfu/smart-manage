package sm.domain.sys.base.login.model.vo;

import lombok.Data;

/** 登录滑块挑战展示数据，不包含服务端答案。 */
@Data
public class CaptchaChallengeVO {
    private String challengeId;
    private String backgroundImage;
    private String templateImage;
    private Integer backgroundImageWidth;
    private Integer backgroundImageHeight;
    private Integer templateImageWidth;
    private Integer templateImageHeight;
}
