package sm.domain.sys.base.menu.model.vo;

import lombok.Data;

/**
 * 菜单工作台所需的应用基础信息。
 */
@Data
public class MenuAppInfoVO {
    private String appName;
    private String appNumber;
    private String appIcon;
    private String domainNumber;
}
