package sm.domain.sys.base.menu.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 获取用户在应用下的菜单树入参
 */
@Data
public class UserMenusByAppIdForm {
	@NotNull(message = "appId不能为空")
	private Long appId;
}

