package sm.domain.scm.procurement.purchaserequisition.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;

@Data
public class PurchaseRequisitionCreateNewDataVO {
    private Long orgId;
    private Long applicantId;
    private LocalDate bizDate;
    private String billStatus;
    private List<PurchaseRequisitionEntryVO> entries = new ArrayList<>();
    private List<AttachmentVO> attachments = new ArrayList<>();
}
