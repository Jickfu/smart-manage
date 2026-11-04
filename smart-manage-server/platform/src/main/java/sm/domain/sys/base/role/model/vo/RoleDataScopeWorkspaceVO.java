package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Schema(description = "角色数据权限配置工作区输出模型")
@Data
public class RoleDataScopeWorkspaceVO {
    @Schema(description = "角色ID")
    private Long roleId;
    @Schema(description = "角色编码")
    private String roleNumber;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "默认数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS")
    private String defaultDataScope;
    @Schema(description = "可配置数据权限的资源列表")
    private Map<String, List<String>> resources;
    @Schema(description = "资源级数据权限规则列表")
    private List<RoleDataScopeRuleVO> rules;
}
