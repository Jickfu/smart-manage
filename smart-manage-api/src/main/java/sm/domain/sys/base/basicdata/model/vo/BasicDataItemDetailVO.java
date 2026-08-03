package sm.domain.sys.base.basicdata.model.vo;

import lombok.Data;

@Data
public class BasicDataItemDetailVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private String number;
    private String name;
    private String remark;
    private Integer sort;
    private Boolean enabled;
    private Boolean systemPreset;
    private Integer level;
    private String numberPath;
    private String namePath;
    private Boolean isLeaf;
    private Integer version;
}
