package sm.domain.sys.base.org.model.vo;

import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgTreeVO {
    private Long id;
    private String number;
    private String name;
    private OrgType orgType;
    private Boolean enabled;
    private Boolean archived;
    private List<OrgTreeVO> children = new ArrayList<>();
}

