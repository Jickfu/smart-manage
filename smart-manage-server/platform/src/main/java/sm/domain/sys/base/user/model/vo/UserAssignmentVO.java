package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

/**
 * 用户任职明细视图。
 */
@Schema(description = "用户任职输出模型")
@Data
public class UserAssignmentVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "任职组织引用")
    private ReferenceVO org;
    @Schema(description = "组织名称路径")
    private String orgNamePath;
    @Schema(description = "任职岗位名称")
    private String position;
    @Schema(description = "是否为部门负责人")
    private Boolean isOrgLeader;
    @Schema(description = "是否为主职")
    private Boolean isPrimary;
}
