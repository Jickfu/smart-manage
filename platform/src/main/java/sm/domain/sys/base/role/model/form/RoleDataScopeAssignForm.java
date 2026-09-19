package sm.domain.sys.base.role.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class RoleDataScopeAssignForm {
    @NotNull private Long roleId;
    @NotNull private Integer version;
    @NotNull
    @Pattern(regexp = "ALL|ORG_AND_CHILDREN|ORG|SELF")
    private String defaultDataScope;
    @NotNull @Valid private List<RoleDataScopeRuleForm> rules = List.of();
}
