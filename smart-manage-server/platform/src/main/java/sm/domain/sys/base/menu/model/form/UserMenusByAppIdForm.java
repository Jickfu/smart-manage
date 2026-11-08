package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 获取用户在应用下的菜单树入参
 */
@Schema(description = "按应用 ID 查询用户菜单输入表单")
@Data
public class UserMenusByAppIdForm {
	@Schema(description = "所属应用ID")
	@NotNull(message = "appId不能为空")
	private Long appId;
}

