package sm.domain.scm.procurement.purchaserequisition.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class PurchaseRequisitionSaveForm {
    private Long id;
    private Integer version;
    @NotBlank(message = "编码不能为空")
    private String number;
    @NotBlank(message = "主题不能为空")
    private String subject;
    @NotNull(message = "业务日期不能为空")
    private LocalDate bizDate;
    private LocalDate requiredDate;
    private String reason;
    /** 当前聚合待确认的临时附件 ID；全局限制由附件配置统一控制。 */
    private List<Long> attachmentIds = List.of();
    /** 临时附件 ID 到上传会话 ID 的映射。 */
    private Map<Long, String> attachmentUploadSessions = Map.of();
    @Valid
    @NotEmpty(message = "采购申请至少需要一条明细")
    private List<PurchaseRequisitionEntryForm> entrys;
}
