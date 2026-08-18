package sm.domain.sys.base.user.apppin.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAppPinForm {
	@NotBlank(message = "应用编码不能为空")
	private String appNumber;
}
