package sm.domain.sys.base.login.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/** 滑块轨迹采样点。 */
@Data
public class CaptchaTrackPointForm {
    @NotNull(message = "轨迹横坐标不能为空")
    @DecimalMin(value = "-100")
    @DecimalMax(value = "5000")
    private Float x;

    @NotNull(message = "轨迹纵坐标不能为空")
    @DecimalMin(value = "-100")
    @DecimalMax(value = "5000")
    private Float y;

    @NotNull(message = "轨迹时间不能为空")
    @DecimalMin(value = "0")
    @DecimalMax(value = "120000")
    private Float t;

    private String type;
}
