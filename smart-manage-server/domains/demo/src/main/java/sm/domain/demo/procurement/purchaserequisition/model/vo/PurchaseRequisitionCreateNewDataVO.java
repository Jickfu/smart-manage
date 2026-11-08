package sm.domain.demo.procurement.purchaserequisition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.attachment.contract.AttachmentReference;

@Schema(description = "采购申请新增默认值输出模型")
@Data
public class PurchaseRequisitionCreateNewDataVO {
    @Schema(description = "组织 ID")
    private Long orgId;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "业务日期")
    private LocalDate bizDate;
    @Schema(description = "明细列表")
    private List<PurchaseRequisitionEntryVO> entries = new ArrayList<>();
    @Schema(description = "业务附件列表")
    private List<AttachmentReference> attachments = new ArrayList<>();
}
