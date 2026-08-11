package sm.domain.sys.base.user.model.vo;

import lombok.Data;

/** 用户任职明细视图。 */
@Data
public class UserAssignmentVO {
    private Long id;
    private Long orgId;
    private String orgName;
    private String orgNamePath;
    private String position;
    private Boolean isOrgLeader;
    private Boolean isPrimary;
}
