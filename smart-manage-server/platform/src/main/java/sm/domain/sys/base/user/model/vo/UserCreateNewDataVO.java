package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户新增默认值
 *
 * @author Chekfu
 */
@Data
@Schema(title = "用户新增默认值")
public class UserCreateNewDataVO {

	@Schema(description = "是否启用")
	private Boolean enabled;

	@Schema(description = "默认角色ID列表")
	private List<Long> defaultRoleIds;
}

