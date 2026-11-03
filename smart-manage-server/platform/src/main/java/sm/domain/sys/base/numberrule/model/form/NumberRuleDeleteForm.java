package sm.domain.sys.base.numberrule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "编号规则删除输入表单")
@Data
public class NumberRuleDeleteForm {
    @Schema(description = "ID")
    @NotNull(message = "编号规则ID不能为空")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}
