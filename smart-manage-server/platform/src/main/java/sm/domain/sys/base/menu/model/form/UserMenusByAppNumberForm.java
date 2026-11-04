package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户在应用下的菜单树入参（按应用编号）
 */
@Schema(description = "按应用编码查询用户菜单输入表单")
@Data
public class UserMenusByAppNumberForm {
	@Schema(description = "业务编码")
	@NotBlank(message = "number不能为空")
	private String number;
}

