package sm.domain.sys.base.app.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按应用编号打开应用入参
 */
@Schema(description = "应用按编码打开输入表单")
@Data
public class AppOpenByNumberForm {
	@Schema(description = "业务编码")
	@NotBlank(message = "应用编号不能为空")
	private String number;
}

