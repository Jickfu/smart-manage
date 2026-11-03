package sm.domain.sys.base.user.quicklaunch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "首页快捷入口配置输出模型")
@Data
@AllArgsConstructor
public class QuickLaunchConfigurationVO {
    @Schema(description = "可选择的快捷入口列表")
    private List<QuickLaunchItemVO> options;
    @Schema(description = "已选择的快捷入口菜单 ID 列表")
    private List<Long> selectedMenuIds;
}
