package sm.domain.demo.procurement.purchaserequisition.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseRequisitionEntryVO {
    private Long id;
    private String materialName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private LocalDate requiredDate;
    private String remark;
    private Integer sort;
}
