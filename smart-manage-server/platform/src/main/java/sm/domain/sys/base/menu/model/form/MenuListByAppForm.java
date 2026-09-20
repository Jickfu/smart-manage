package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按应用查询菜单列表表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "按应用查询菜单")
public class MenuListByAppForm {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long appId;
}
