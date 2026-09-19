package sm.domain.sys.base.user.quicklaunch.model.vo;

import lombok.Data;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;

@Data
public class QuickLaunchItemVO {
    private Long menuId;
    private String menuNumber;
    private String name;
    private String icon;
    private String appNumber;
    private String appName;
    private String groupName;
    private String component;
    private MenuTargetTypeEnum targetType;
    private String externalUrl;
    private ExternalOpenModeEnum externalOpenMode;
}
