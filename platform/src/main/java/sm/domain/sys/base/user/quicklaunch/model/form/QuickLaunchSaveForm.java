package sm.domain.sys.base.user.quicklaunch.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuickLaunchSaveForm extends QuickLaunchScopeForm {
    @NotNull(message = "快捷菜单不能为空")
    private List<Long> menuIds;
}
