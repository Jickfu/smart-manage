package sm.domain.demo.procurement.purchaserequisition.model.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseRequisitionEntryForm {
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String specification;
    @NotBlank(message = "单位不能为空")
    private String unit;
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "数量必须大于0")
    private BigDecimal quantity;
    private LocalDate requiredDate;
    private String remark;
    private Integer sort;
}
