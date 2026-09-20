package sm.domain.sys.base.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author Chekfu
 */
@Getter
public enum MenuLevelEnum {
	CATEGORY(0, "分组"),
	PAGE(1, "页面"),
	;

	@EnumValue
	@JsonValue
	private final int code;
	private final String desc;

	MenuLevelEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
