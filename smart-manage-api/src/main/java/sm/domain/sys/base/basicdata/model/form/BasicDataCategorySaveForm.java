package sm.domain.sys.base.basicdata.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BasicDataCategorySaveForm {
    private Long id;
    private Integer version;
    @NotNull(message = "所属领域不能为空")
    private Long domainId;
    @NotBlank(message = "编码不能为空")
    @Size(max = 64, message = "编码不能超过64个字符")
    private String number;
    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称不能超过128个字符")
    private String name;
    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;
    private Boolean enabled;
    @NotBlank(message = "编号模式不能为空")
    private String numberMode;
    @NotBlank(message = "编号规则不能为空")
    @Size(max = 200, message = "编号规则键不能超过200个字符")
    private String numberRuleKey;
}
