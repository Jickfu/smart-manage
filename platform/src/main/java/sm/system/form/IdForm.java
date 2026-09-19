package sm.system.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "主键 id")
public class IdForm {
	@NotNull(message = "id 不能为空")
	private Long id;
}
