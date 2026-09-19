package sm.domain.sys.base.org.model.vo;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.domain.sys.base.org.model.OrgType;

@Data
public class OrgDetailVO {
    private Long id;
    private String number;
    private String name;
    private ReferenceVO parent;
    private String numberPath;
    private String namePath;
    private OrgType orgType;
    private Integer sort;
    private Boolean enabled;
    private Boolean archived;
    private String description;
    private Integer version;
}
