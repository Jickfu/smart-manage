package sm.domain.scm.procurement.purchaserequisition.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    @Valid
    @NotEmpty(message = "采购申请至少需要一条明细")
    private List<PurchaseRequisitionEntryForm> entrys;
}
