package sm.domain.sys.scheduler.singleton.model.form;
import lombok.Data;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
/** 常驻任务启停命令。 */
@Data
@Schema(description = "常驻任务启停命令")
public class SingletonCommandForm {
    @NotBlank @Size(max = 120)
    @Schema(description = "任务编码")
    private String number;
    @NotNull @Min(0)
    @Schema(description = "当前启停版本")
    private Integer version;
    @NotNull
    @Schema(description = "期望是否运行")
    private Boolean requested;
}
