package sm.domain.sys.base.menu.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户在应用下的菜单树入参（按应用编号）
 */
@Data
public class UserMenusByAppNumberForm {
	@NotBlank(message = "number不能为空")
	private String number;
}

