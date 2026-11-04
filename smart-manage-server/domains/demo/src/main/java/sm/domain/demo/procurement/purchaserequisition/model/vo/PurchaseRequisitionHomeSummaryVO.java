package sm.domain.demo.procurement.purchaserequisition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Schema(description = "采购申请首页摘要输出模型")
@Data
public class PurchaseRequisitionHomeSummaryVO {
    @Schema(description = "按采购申请单据状态分组的数量")
    private Map<String, Long> statusCounts;
    @Schema(description = "最近采购申请列表")
    private List<PurchaseRequisitionListVO> recent;
}
