package sm.domain.sys.monitor.script.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "脚本保存表单")
public class ScriptSaveForm {
    @Schema(description = "主键ID（新建时不传）")
    private Long id;

    @Schema(description = "乐观锁版本号，修改时必传")
    private Integer version;

    @NotBlank(message = "编码不能为空")
    @Size(max = 100, message = "编码不能超过100个字符")
    @Schema(description = "编码")
    private String number;

    @NotBlank(message = "名称不能为空")
    @Size(max = 200, message = "名称不能超过200个字符")
    @Schema(description = "名称")
    private String name;

    @NotBlank(message = "脚本内容不能为空")
    @Size(max = 1000000, message = "脚本内容不能超过1000000个字符")
    @Schema(description = "脚本内容")
    private String content;

    @Schema(description = "描述")
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}
