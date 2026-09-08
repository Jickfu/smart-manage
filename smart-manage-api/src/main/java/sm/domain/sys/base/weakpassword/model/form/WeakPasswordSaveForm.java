package sm.domain.sys.base.weakpassword.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WeakPasswordSaveForm {
    private Long id;
    private Integer version;
    @NotBlank(message = "弱口令不能为空")
    private String word;
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}
