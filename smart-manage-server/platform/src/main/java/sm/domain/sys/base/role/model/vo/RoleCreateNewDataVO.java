package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色新增默认值
 */
@Data
@Schema(description = "角色新增默认值")
public class RoleCreateNewDataVO {
	@Schema(description = "默认数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS")
	private String defaultDataScope = "SELF";
}

