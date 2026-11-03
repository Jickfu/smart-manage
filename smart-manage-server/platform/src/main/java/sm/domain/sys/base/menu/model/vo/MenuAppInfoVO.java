package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 菜单工作台所需的应用基础信息。
 */
@Schema(description = "菜单应用信息输出模型")
@Data
public class MenuAppInfoVO {
    @Schema(description = "所属应用名称")
    private String appName;
    @Schema(description = "应用编码")
    private String appNumber;
    @Schema(description = "应用图标")
    private String appIcon;
    @Schema(description = "所属领域编号（t_sys_domain.number）")
    private String domainNumber;
}
