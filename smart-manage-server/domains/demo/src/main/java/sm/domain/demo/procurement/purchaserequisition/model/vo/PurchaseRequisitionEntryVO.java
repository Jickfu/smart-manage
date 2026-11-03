package sm.domain.demo.procurement.purchaserequisition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "采购申请明细输出模型")
@Data
public class PurchaseRequisitionEntryVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "物料名称")
    private String materialName;
    @Schema(description = "规格型号")
    private String specification;
    @Schema(description = "单位")
    private String unit;
    @Schema(description = "数量")
    private BigDecimal quantity;
    @Schema(description = "需求日期")
    private LocalDate requiredDate;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "排序号")
    private Integer sort;
}
