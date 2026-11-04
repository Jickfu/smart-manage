package sm.domain.demo.procurement.purchaserequisition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import sm.system.excel.DataExportLayout;

@Schema(description = "采购申请导出输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionExportForm extends PurchaseRequisitionListForm {
    /**
     * 非空时导出选中记录；为空时导出当前筛选结果
     */
    @Schema(description = "ID 列表")
    private List<Long> ids = new ArrayList<>();
    @Schema(description = "导出文件布局")
    @NotNull(message = "导出布局不能为空")
    private DataExportLayout layout;
}
