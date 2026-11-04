package sm.domain.demo.procurement.purchaserequisition.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "采购申请保存输入表单")
@Data
public class PurchaseRequisitionSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    private Integer version;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "主题")
    @NotBlank(message = "主题不能为空")
    private String subject;
    @Schema(description = "业务日期")
    @NotNull(message = "业务日期不能为空")
    private LocalDate bizDate;
    @Schema(description = "需求日期")
    private LocalDate requiredDate;
    @Schema(description = "申请原因")
    private String reason;
    /**
     * 当前聚合待确认的临时附件 ID；全局限制由附件配置统一控制
     */
    @Schema(description = "附件 ID 列表")
    private List<Long> attachmentIds = List.of();
    /**
     * 临时附件 ID 到上传会话 ID 的映射
     */
    @Schema(description = "附件上传会话映射，键为附件 ID，值为上传会话标识；用于确认临时附件归属")
    private Map<Long, String> attachmentUploadSessions = Map.of();
    @Schema(description = "明细列表")
    @Valid
    @NotEmpty(message = "采购申请至少需要一条明细")
    private List<PurchaseRequisitionEntryForm> entries;
}
