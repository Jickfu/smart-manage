package sm.domain.sys.scheduler.singleton.model.form;
import lombok.Data;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
/** 常驻任务详情输入。 */
@Data
@Schema(description = "常驻任务详情输入")
public class SingletonDetailForm {
    @NotBlank @Size(max = 120)
    @Schema(description = "任务编码")
    private String number;
}
