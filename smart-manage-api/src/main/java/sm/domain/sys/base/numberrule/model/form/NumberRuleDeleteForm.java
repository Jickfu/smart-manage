package sm.domain.sys.base.numberrule.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NumberRuleDeleteForm {
    @NotNull(message = "编号规则ID不能为空")
    private Long id;
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}
