package sm.domain.scm.procurement.purchaserequisition.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;

@Data
public class PurchaseRequisitionDetailVO {
    private Long id;
    private Integer version;
    private String number;
    private String subject;
    private Long orgId;
    private Long applicantId;
    private LocalDate bizDate;
    private LocalDate requiredDate;
    private String reason;
    private String billStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PurchaseRequisitionEntryVO> entrys = new ArrayList<>();
    private List<AttachmentVO> attachments = new ArrayList<>();
}
