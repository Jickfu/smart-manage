package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 设置、切换或取消默认角色的独立命令。 */
@Data
@Schema(description = "默认角色设置输入表单")
public class RoleDefaultSaveForm {
    @Schema(description = "目标角色ID，空表示取消默认角色")
    private Long roleId;
    @Schema(description = "操作前默认角色ID，空表示原先未设置，用于校验和审计")
    private Long previousRoleId;
    @NotNull
    @Min(0)
    @Schema(description = "操作前配置并发版本号")
    private Integer version;
}
