package sm.domain.sys.base.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 登录滑块挑战展示数据，不包含服务端答案。
 */
@Schema(description = "验证码挑战输出模型")
@Data
public class CaptchaChallengeVO {
    @Schema(description = "验证码挑战标识")
    private String challengeId;
    @Schema(description = "验证码背景图")
    private String backgroundImage;
    @Schema(description = "验证码滑块图")
    private String templateImage;
    @Schema(description = "验证码背景图宽度，单位像素")
    private Integer backgroundImageWidth;
    @Schema(description = "验证码背景图高度，单位像素")
    private Integer backgroundImageHeight;
    @Schema(description = "验证码滑块图宽度，单位像素")
    private Integer templateImageWidth;
    @Schema(description = "验证码滑块图高度，单位像素")
    private Integer templateImageHeight;
}
