package sm.domain.sys.base.role.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class RoleDataScopeRuleVO {
    private String resourceType;
    private String action;
    private String scopeType;
    private List<Long> orgIds;
}
