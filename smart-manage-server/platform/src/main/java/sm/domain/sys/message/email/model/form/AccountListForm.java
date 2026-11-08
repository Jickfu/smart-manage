package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "发信账号列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "是否启用")
    private Boolean enabled;
}
