package sm.domain.demo.procurement.purchaserequisition.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionListForm extends PageForm {
    private String keyword;
    private String billStatus;
}
