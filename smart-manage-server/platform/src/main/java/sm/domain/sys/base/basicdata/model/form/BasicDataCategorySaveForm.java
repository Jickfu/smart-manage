package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "基础资料分类保存输入表单")
@Data
public class BasicDataCategorySaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "所属领域ID")
    @NotNull(message = "所属领域不能为空")
    private Long domainId;
    @Schema(description = "业务编码")
    @NotBlank(message = "编码不能为空")
    @Size(max = 64, message = "编码不能超过64个字符")
    private String number;
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称不能超过128个字符")
    private String name;
    @Schema(description = "说明")
    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "节点编号模式：MANUAL人工、AUTO_LOCKED自动锁定、AUTO_DEFAULT留空自动")
    @NotBlank(message = "编号模式不能为空")
    private String numberMode;
    @Schema(description = "节点编号规则键")
    @NotBlank(message = "编号规则不能为空")
    @Size(max = 200, message = "编号规则键不能超过200个字符")
    private String numberRuleKey;
}
