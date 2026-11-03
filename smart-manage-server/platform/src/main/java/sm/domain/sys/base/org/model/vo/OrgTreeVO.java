package sm.domain.sys.base.org.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "组织树节点输出模型")
@Data
public class OrgTreeVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "组织类型：GROUP 集团、COMPANY 公司、DEPARTMENT 部门")
    private OrgType orgType;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "封存状态")
    private Boolean archived;
    @Schema(description = "子节点列表")
    private List<OrgTreeVO> children = new ArrayList<>();
}

