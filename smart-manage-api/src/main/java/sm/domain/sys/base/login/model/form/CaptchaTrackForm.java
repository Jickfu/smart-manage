package sm.domain.sys.base.login.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/** 滑块最终位置及拖动轨迹。 */
@Data
public class CaptchaTrackForm {
    @NotNull @Min(1) @Max(2000) private Integer backgroundImageWidth;
    @NotNull @Min(1) @Max(2000) private Integer backgroundImageHeight;
    @NotNull @Min(1) @Max(2000) private Integer templateImageWidth;
    @NotNull @Min(1) @Max(2000) private Integer templateImageHeight;
    @NotNull @Min(1) private Long startTime;
    @NotNull @Min(1) private Long stopTime;
    @NotNull @Min(0) @Max(2000) private Integer left;
    @NotNull @Min(0) @Max(2000) private Integer top;

    @Size(min = 2, max = 512, message = "滑块轨迹采样点数量应在2至512之间")
    private List<@Valid CaptchaTrackPointForm> trackList;
}
