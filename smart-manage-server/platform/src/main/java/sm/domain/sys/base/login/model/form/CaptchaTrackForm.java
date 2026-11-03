package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 滑块最终位置及拖动轨迹。
 */
@Schema(description = "验证码拖动轨迹输入表单")
@Data
public class CaptchaTrackForm {
    @Schema(description = "验证码背景图宽度，单位像素")
    @NotNull @Min(1) @Max(2000) private Integer backgroundImageWidth;
    @Schema(description = "验证码背景图高度，单位像素")
    @NotNull @Min(1) @Max(2000) private Integer backgroundImageHeight;
    @Schema(description = "验证码滑块图宽度，单位像素")
    @NotNull @Min(1) @Max(2000) private Integer templateImageWidth;
    @Schema(description = "验证码滑块图高度，单位像素")
    @NotNull @Min(1) @Max(2000) private Integer templateImageHeight;
    @Schema(description = "拖动开始时间，Unix 时间戳，单位毫秒")
    @NotNull @Min(1) private Long startTime;
    @Schema(description = "拖动结束时间，Unix 时间戳，单位毫秒")
    @NotNull @Min(1) private Long stopTime;
    @Schema(description = "验证码组件左侧坐标，单位像素")
    @NotNull @Min(0) @Max(2000) private Integer left;
    @Schema(description = "验证码组件顶部坐标，单位像素")
    @NotNull @Min(0) @Max(2000) private Integer top;

    @Schema(description = "按时间顺序排列的轨迹点")
    @Size(min = 2, max = 512, message = "滑块轨迹采样点数量应在2至512之间")
    private List<@Valid CaptchaTrackPointForm> trackList;
}
