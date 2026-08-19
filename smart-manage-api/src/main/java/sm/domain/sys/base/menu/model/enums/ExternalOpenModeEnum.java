package sm.domain.sys.base.menu.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/** 外部链接打开方式。 */
@Getter
public enum ExternalOpenModeEnum {
    NEW_TAB("NEW_TAB", "新浏览器标签页"),
    IFRAME("IFRAME", "工作台内嵌页");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    ExternalOpenModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
