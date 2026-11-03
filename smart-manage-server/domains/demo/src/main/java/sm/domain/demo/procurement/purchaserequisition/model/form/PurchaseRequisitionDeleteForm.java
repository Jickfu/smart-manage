package sm.domain.demo.procurement.purchaserequisition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除采购申请时携带列表读取到的版本，保证状态与并发校验原子生效。
 */
@Schema(description = "采购申请删除输入表单")
@Data
public class PurchaseRequisitionDeleteForm {

    @Schema(description = "ID")
    @NotNull(message = "采购申请ID不能为空")
    private Long id;

    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "采购申请版本号不能为空")
    private Integer version;
}
