package sm.domain.sys.base.role.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class RoleDataScopeRuleForm {
    @NotBlank private String resourceType;
    private String action;
    @NotBlank
    @Pattern(regexp = "ALL|ORG_AND_CHILDREN|ORG|SELF|CUSTOM_ORGS")
    private String scopeType;
    @NotNull private List<Long> orgIds = List.of();
}
