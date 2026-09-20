package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户权限查询表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "用户权限查询表单")
public class UserPermissionsForm {

    @Schema(description = "权限编码前缀")
    @NotBlank(message = "前缀不能为空")
    private String prefix;
}
