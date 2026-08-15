package sm.domain.sys.base.basicdata.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BasicDataItemSaveForm {
    private Long id;
    private Integer version;
    @NotNull(message = "基础资料分类不能为空")
    private Long categoryId;
    private Long parentId;
    @Size(max = 64, message = "编码不能超过64个字符")
    private String number;
    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称不能超过128个字符")
    private String name;
    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;
    private Integer sort;
    private Boolean enabled;
}
