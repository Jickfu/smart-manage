package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/**
 * 滑块轨迹采样点。
 */
@Schema(description = "验证码轨迹采样点输入表单")
@Data
public class CaptchaTrackPointForm {
    @Schema(description = "轨迹点横坐标")
    @NotNull(message = "轨迹横坐标不能为空")
    @DecimalMin(value = "-100")
    @DecimalMax(value = "5000")
    private Float x;

    @Schema(description = "轨迹点纵坐标")
    @NotNull(message = "轨迹纵坐标不能为空")
    @DecimalMin(value = "-100")
    @DecimalMax(value = "5000")
    private Float y;

    @Schema(description = "轨迹采样点相对时间，单位毫秒")
    @NotNull(message = "轨迹时间不能为空")
    @DecimalMin(value = "0")
    @DecimalMax(value = "120000")
    private Float t;

    @Schema(description = "验证码轨迹事件类型")
    private String type;
}
