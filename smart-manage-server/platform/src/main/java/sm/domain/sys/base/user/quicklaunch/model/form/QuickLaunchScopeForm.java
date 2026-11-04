package sm.domain.sys.base.user.quicklaunch.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;

@Schema(description = "首页快捷入口范围输入表单")
@Data
public class QuickLaunchScopeForm {
    @Schema(description = "首页范围")
    @NotNull(message = "首页范围不能为空")
    private HomeScopeEnum scope;
    @Schema(description = "应用编码")
    private String appNumber;
}
