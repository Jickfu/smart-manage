package sm.domain.sys.base.user.model.vo;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

/** 用户任职明细视图。 */
@Data
public class UserAssignmentVO {
    private Long id;
    private ReferenceVO org;
    private String orgNamePath;
    private String position;
    private Boolean isOrgLeader;
    private Boolean isPrimary;
}
