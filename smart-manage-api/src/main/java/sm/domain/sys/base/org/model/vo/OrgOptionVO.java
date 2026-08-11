package sm.domain.sys.base.org.model.vo;

import lombok.Data;

@Data
public class OrgOptionVO {
    private Long id;
    private String number;
    private String name;
    private Long parentId;
    private String namePath;
}

