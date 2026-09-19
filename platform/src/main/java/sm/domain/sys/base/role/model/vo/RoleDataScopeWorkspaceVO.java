package sm.domain.sys.base.role.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RoleDataScopeWorkspaceVO {
    private Long roleId;
    private String roleNumber;
    private String roleName;
    private Integer version;
    private String defaultDataScope;
    private Map<String, List<String>> resources;
    private List<RoleDataScopeRuleVO> rules;
}
