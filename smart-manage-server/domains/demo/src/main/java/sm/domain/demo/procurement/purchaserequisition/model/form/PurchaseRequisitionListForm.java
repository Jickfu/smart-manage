package sm.domain.demo.procurement.purchaserequisition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "采购申请列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "单据状态：A暂存，B已提交，C已审核，D已关闭")
    private String billStatus;
}
