package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 基础数据明细表单。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "基础数据明细表单")
public class BasicDataEntryForm {

    @Schema(description = "明细ID（新增明细时不传）")
    private Long id;

    @NotBlank(message = "明细编码不能为空")
    @Size(max = 64, message = "明细编码不能超过64个字符")
    @Schema(description = "编码")
    private String number;

    @NotBlank(message = "明细名称不能为空")
    @Size(max = 128, message = "明细名称不能超过128个字符")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean enabled;

}
