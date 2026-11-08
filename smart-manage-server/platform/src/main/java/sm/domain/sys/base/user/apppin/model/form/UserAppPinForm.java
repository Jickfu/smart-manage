package sm.domain.sys.base.user.apppin.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "用户固定应用输入表单")
@Data
public class UserAppPinForm {
	@Schema(description = "应用编码")
	@NotBlank(message = "应用编码不能为空")
	private String appNumber;
}
