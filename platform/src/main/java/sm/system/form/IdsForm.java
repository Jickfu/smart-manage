package sm.system.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量 ID 命令表单。
 *
 * @author Chekfu
 */
@Data
public class IdsForm {
	@NotEmpty(message = "ID列表不能为空")
	private List<@NotNull(message = "ID不能为空") Long> ids;
}
