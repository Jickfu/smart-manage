package sm.domain.sys.base.user.quicklaunch.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "首页快捷入口保存输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickLaunchSaveForm extends QuickLaunchScopeForm {
    @Schema(description = "选中的菜单 ID 列表")
    @NotNull(message = "快捷菜单不能为空")
    private List<Long> menuIds;
}
