package sm.domain.sys.base.user.quicklaunch.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;

@Data
public class QuickLaunchScopeForm {
    @NotNull(message = "首页范围不能为空")
    private HomeScopeEnum scope;
    private String appNumber;
}
