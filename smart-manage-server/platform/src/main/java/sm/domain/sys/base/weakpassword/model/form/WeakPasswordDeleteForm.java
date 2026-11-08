package sm.domain.sys.base.weakpassword.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "弱密码删除输入表单")
@Data
public class WeakPasswordDeleteForm {
    @Schema(description = "ID")
    @NotNull(message = "弱口令ID不能为空")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
