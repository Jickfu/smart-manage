package sm.domain.sys.base.basicdata.model.vo;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

@Data
public class BasicDataItemDetailVO {
    private Long id;
    private ReferenceVO category;
    private ReferenceVO parent;
    private String number;
    private String name;
    private String description;
    private Integer sort;
    private Boolean enabled;
    private Boolean systemPreset;
    private Integer level;
    private String numberPath;
    private String namePath;
    private Boolean isLeaf;
    private Integer version;
}
