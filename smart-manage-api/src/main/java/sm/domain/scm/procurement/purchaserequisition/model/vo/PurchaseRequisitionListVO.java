package sm.domain.scm.procurement.purchaserequisition.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PurchaseRequisitionListVO {
    private Long id;
    private Integer version;
    private String number;
    private String subject;
    private LocalDate bizDate;
    private LocalDate requiredDate;
    private String billStatus;
    private LocalDateTime createTime;
}
