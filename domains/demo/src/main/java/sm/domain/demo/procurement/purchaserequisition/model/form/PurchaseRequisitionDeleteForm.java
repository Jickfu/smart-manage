package sm.domain.demo.procurement.purchaserequisition.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 删除采购申请时携带列表读取到的版本，保证状态与并发校验原子生效。 */
@Data
public class PurchaseRequisitionDeleteForm {

    @NotNull(message = "采购申请ID不能为空")
    private Long id;

    @NotNull(message = "采购申请版本号不能为空")
    private Integer version;
}
