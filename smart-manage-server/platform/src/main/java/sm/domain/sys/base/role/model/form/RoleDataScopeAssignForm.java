package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Schema(description = "角色数据权限分配输入表单")
@Data
public class RoleDataScopeAssignForm {
    @Schema(description = "角色ID")
    @NotNull private Long roleId;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull private Integer version;
    @Schema(description = "默认数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS")
    @NotNull
    @Pattern(regexp = "ALL|ORG_AND_CHILDREN|ORG|SELF")
    private String defaultDataScope;
    @Schema(description = "资源级数据权限规则列表")
    @NotNull @Valid private List<RoleDataScopeRuleForm> rules = List.of();
}
