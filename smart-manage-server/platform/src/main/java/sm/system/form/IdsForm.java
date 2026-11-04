package sm.system.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量 ID 命令表单。
 *
 * @author Chekfu
 */
@Schema(description = "主键列表输入表单")
@Data
public class IdsForm {
	@Schema(description = "ID 列表")
	@NotEmpty(message = "ID列表不能为空")
	private List<@NotNull(message = "ID不能为空") Long> ids;
}
