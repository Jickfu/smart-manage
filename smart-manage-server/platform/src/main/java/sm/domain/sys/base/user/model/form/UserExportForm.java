package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.excel.DataExportLayout;

import java.util.List;

@Schema(description = "用户导出输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserExportForm extends UserListForm {
    @Schema(description = "导出文件布局")
    @NotNull(message = "导出布局不能为空")
    private DataExportLayout layout;
    @Schema(description = "ID 列表")
    private List<Long> ids;
}
