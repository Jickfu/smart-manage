package sm.domain.demo.procurement.purchaserequisition.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "采购申请列表输出模型")
@Data
public class PurchaseRequisitionListVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "主题")
    private String subject;
    @Schema(description = "业务日期")
    private LocalDate bizDate;
    @Schema(description = "需求日期")
    private LocalDate requiredDate;
    @Schema(description = "单据状态：A暂存，B已提交，C已审核，D已关闭")
    private String billStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
