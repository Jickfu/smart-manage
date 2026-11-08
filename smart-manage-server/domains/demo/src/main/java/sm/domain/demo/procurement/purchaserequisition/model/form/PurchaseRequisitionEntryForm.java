package sm.domain.demo.procurement.purchaserequisition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "采购申请明细输入表单")
@Data
public class PurchaseRequisitionEntryForm {
    @Schema(description = "物料名称")
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    @Schema(description = "规格型号")
    private String specification;
    @Schema(description = "单位")
    @NotBlank(message = "单位不能为空")
    private String unit;
    @Schema(description = "数量")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "数量必须大于0")
    private BigDecimal quantity;
    @Schema(description = "需求日期")
    private LocalDate requiredDate;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "排序号")
    private Integer sort;
}
