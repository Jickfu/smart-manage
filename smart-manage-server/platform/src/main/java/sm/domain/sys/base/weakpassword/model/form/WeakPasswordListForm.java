package sm.domain.sys.base.weakpassword.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "弱密码列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class WeakPasswordListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
}
