package sm.domain.sys.base.menu.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/** 页面菜单目标类型。 */
@Getter
public enum MenuTargetTypeEnum {
    INTERNAL_PAGE("INTERNAL_PAGE", "内部页面"),
    EXTERNAL_LINK("EXTERNAL_LINK", "外部链接");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    MenuTargetTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
