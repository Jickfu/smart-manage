package sm.domain.demo.procurement.purchaserequisition.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.attachment.contract.AttachmentReference;

@Data
public class PurchaseRequisitionCreateNewDataVO {
    private Long orgId;
    private Long applicantId;
    private LocalDate bizDate;
    private List<PurchaseRequisitionEntryVO> entries = new ArrayList<>();
    private List<AttachmentReference> attachments = new ArrayList<>();
}
