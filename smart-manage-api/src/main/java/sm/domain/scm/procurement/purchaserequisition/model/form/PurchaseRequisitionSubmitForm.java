package sm.domain.scm.procurement.purchaserequisition.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 采购申请提交参数，提交命令必须携带客户端读取到的乐观锁版本。 */
@Data
public class PurchaseRequisitionSubmitForm {
    @NotNull
    private Long id;
    @NotNull
    private Integer version;
}
