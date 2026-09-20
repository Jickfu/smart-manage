package sm.domain.sys.base.app.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按应用编号打开应用入参
 */
@Data
public class AppOpenByNumberForm {
	@NotBlank(message = "应用编号不能为空")
	private String number;
}

