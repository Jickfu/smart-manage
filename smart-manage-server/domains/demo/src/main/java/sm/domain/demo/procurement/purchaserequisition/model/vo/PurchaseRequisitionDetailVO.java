package sm.domain.demo.procurement.purchaserequisition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.attachment.contract.AttachmentReference;

@Schema(description = "采购申请详情输出模型")
@Data
public class PurchaseRequisitionDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "主题")
    private String subject;
    @Schema(description = "组织 ID")
    private Long orgId;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "业务日期")
    private LocalDate bizDate;
    @Schema(description = "需求日期")
    private LocalDate requiredDate;
    @Schema(description = "申请原因")
    private String reason;
    @Schema(description = "单据状态：A暂存，B已提交，C已审核，D已关闭")
    private String billStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "明细列表")
    private List<PurchaseRequisitionEntryVO> entries = new ArrayList<>();
    @Schema(description = "业务附件列表")
    private List<AttachmentReference> attachments = new ArrayList<>();
}
