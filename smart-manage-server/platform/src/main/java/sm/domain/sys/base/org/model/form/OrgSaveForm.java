package sm.domain.sys.base.org.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sm.domain.sys.base.org.model.OrgType;

@Schema(description = "组织保存输入表单")
@Data
public class OrgSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "业务编码")
    @NotBlank(message = "编码不能为空")
    @Size(max = 100, message = "编码不能超过100个字符")
    private String number;
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;
    @Schema(description = "父ID")
    private Long parentId;
    @Schema(description = "组织类型：GROUP 集团、COMPANY 公司、DEPARTMENT 部门")
    @NotNull(message = "组织类型不能为空")
    private OrgType orgType;
    @Schema(description = "排序号")
    @Min(value = 0, message = "排序不能小于0")
    @Max(value = 999999, message = "排序不能超过999999")
    private Integer sort;
    @Schema(description = "说明")
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}

