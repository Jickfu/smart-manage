package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色新增默认值
 */
@Data
@Schema(title = "角色新增默认值")
public class RoleCreateNewDataVO {
	private String defaultDataScope = "SELF";
}

