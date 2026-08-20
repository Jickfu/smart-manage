package sm.domain.sys.base.basicdata.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BasicDataListVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long parentId;
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
    private LocalDateTime updateTime;
}
