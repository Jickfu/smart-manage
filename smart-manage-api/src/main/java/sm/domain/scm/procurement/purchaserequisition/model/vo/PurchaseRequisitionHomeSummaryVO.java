package sm.domain.scm.procurement.purchaserequisition.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PurchaseRequisitionHomeSummaryVO {
    private Map<String, Long> statusCounts;
    private List<PurchaseRequisitionListVO> recent;
}
