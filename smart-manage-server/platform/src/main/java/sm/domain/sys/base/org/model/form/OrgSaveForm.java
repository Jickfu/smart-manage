package sm.domain.sys.base.org.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

@Data
public class OrgSaveForm {
    private Long id;
    private Integer version;
    @NotBlank(message = "编码不能为空")
    @Size(max = 100, message = "编码不能超过100个字符")
    private String number;
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;
    private Long parentId;
    @NotNull(message = "组织类型不能为空")
    private OrgType orgType;
    @Min(value = 0, message = "排序不能小于0")
    @Max(value = 999999, message = "排序不能超过999999")
    private Integer sort;
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}

