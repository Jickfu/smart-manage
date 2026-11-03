package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Schema(description = "角色数据权限规则输入表单")
@Data
public class RoleDataScopeRuleForm {
    @Schema(description = "资源类型")
    @NotBlank private String resourceType;
    @Schema(description = "数据权限操作标识")
    private String action;
    @Schema(description = "数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS")
    @NotBlank
    @Pattern(regexp = "ALL|ORG_AND_CHILDREN|ORG|SELF|CUSTOM_ORGS")
    private String scopeType;
    @Schema(description = "指定组织 ID 列表")
    @NotNull private List<Long> orgIds = List.of();
}
