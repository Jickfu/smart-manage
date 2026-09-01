package sm.domain.scm.procurement.purchaserequisition.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import sm.system.excel.DataExportLayout;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionExportForm extends PurchaseRequisitionListForm {
    /** 非空时导出选中记录；为空时导出当前筛选结果。 */
    private List<Long> ids = new ArrayList<>();
    @NotNull(message = "导出布局不能为空")
    private DataExportLayout layout;
}
