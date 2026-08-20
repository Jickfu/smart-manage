package sm.domain.sys.base.sysparam.model.vo;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

@Data
public class SysParamDetailVO {
    private Long id;
    private Integer version;
    private String number;
    private String name;
    private String value;
    private String description;
    private Boolean isSystem;
    private ReferenceVO application;
}
