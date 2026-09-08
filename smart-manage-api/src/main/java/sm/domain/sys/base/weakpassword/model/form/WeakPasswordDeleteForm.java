package sm.domain.sys.base.weakpassword.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeakPasswordDeleteForm {
    @NotNull(message = "弱口令ID不能为空")
    private Long id;
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
