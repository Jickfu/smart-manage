package sm.domain.sys.base.org.model.vo;

import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

import java.time.LocalDateTime;

@Data
public class OrgListVO {
    private Long id;
    private String number;
    private String name;
    private Long parentId;
    private String numberPath;
    private String namePath;
    private OrgType orgType;
    private Integer sort;
    private Boolean enabled;
    private Boolean archived;
    private LocalDateTime archivedAt;
    private String description;
    private Integer version;
}
