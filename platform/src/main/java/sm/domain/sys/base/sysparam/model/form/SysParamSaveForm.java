package sm.domain.sys.base.sysparam.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统参数保存表单
 *
 * @author Chekfu
 */
@Data
@Schema(description = "系统参数保存表单")
public class SysParamSaveForm {

    @Schema(description = "主键ID（新建时不传）")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必传")
    private Integer version;

    @NotBlank(message = "参数编码不能为空")
    @Schema(description = "参数编码")
    private String number;

    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @Schema(description = "参数值")
    private String value;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "所属功能ID，留空表示全局参数")
    private Long featureId;
}
