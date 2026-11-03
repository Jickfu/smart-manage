package sm.domain.sys.base.org.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

import java.time.LocalDateTime;

@Schema(description = "组织列表输出模型")
@Data
public class OrgListVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "父ID")
    private Long parentId;
    @Schema(description = "长编码")
    private String numberPath;
    @Schema(description = "长名称")
    private String namePath;
    @Schema(description = "组织类型：GROUP 集团、COMPANY 公司、DEPARTMENT 部门")
    private OrgType orgType;
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "封存状态")
    private Boolean archived;
    @Schema(description = "封存时间")
    private LocalDateTime archivedAt;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
