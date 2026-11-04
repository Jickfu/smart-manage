package sm.domain.sys.base.user.quicklaunch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;

@Schema(description = "首页快捷入口条目输出模型")
@Data
public class QuickLaunchItemVO {
    @Schema(description = "菜单ID")
    private Long menuId;
    @Schema(description = "菜单编码")
    private String menuNumber;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "应用编码")
    private String appNumber;
    @Schema(description = "所属应用名称")
    private String appName;
    @Schema(description = "快捷入口分组名称")
    private String groupName;
    @Schema(description = "组件")
    private String component;
    @Schema(description = "页面目标类型")
    private MenuTargetTypeEnum targetType;
    @Schema(description = "外部链接地址")
    private String externalUrl;
    @Schema(description = "外部链接打开方式")
    private ExternalOpenModeEnum externalOpenMode;
}
