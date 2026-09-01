package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.excel.DataExportLayout;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserExportForm extends UserListForm {
    @NotNull(message = "导出布局不能为空")
    private DataExportLayout layout;
    private List<Long> ids;
}
