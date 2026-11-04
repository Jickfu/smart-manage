package sm.domain.sys.base.weakpassword.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "弱密码保存输入表单")
@Data
public class WeakPasswordSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "完整弱口令词条")
    @NotBlank(message = "弱口令不能为空")
    private String word;
    @Schema(description = "说明")
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}
