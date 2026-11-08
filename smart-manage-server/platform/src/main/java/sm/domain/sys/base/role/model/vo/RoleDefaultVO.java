package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

/** 默认角色配置快照。 */
@Data
@Schema(description = "默认角色配置输出模型")
public class RoleDefaultVO {
    @Schema(description = "默认角色，空表示未设置")
    private ReferenceVO role;
    @Schema(description = "配置并发版本号")
    private Integer version;
}
