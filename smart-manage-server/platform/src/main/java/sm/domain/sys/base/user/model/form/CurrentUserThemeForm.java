package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "当前用户主题配置")
public class CurrentUserThemeForm {

    @NotBlank(message = "主题色不能为空")
    @Schema(description = "预置主题色", requiredMode = Schema.RequiredMode.REQUIRED)
    private String themeColor;
}
