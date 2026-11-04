package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;

@Schema(description = "角色数据权限规则输出模型")
@Data
public class RoleDataScopeRuleVO {
    @Schema(description = "资源类型")
    private String resourceType;
    @Schema(description = "数据权限操作标识")
    private String action;
    @Schema(description = "数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS")
    private String scopeType;
    @Schema(description = "指定组织 ID 列表")
    private List<Long> orgIds;
}
