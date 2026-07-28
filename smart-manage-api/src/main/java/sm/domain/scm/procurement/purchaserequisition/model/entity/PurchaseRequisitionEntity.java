package sm.domain.scm.procurement.purchaserequisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseBillEntity;

import java.time.LocalDate;

/** 采购申请聚合根。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_scm_purchase_requisition")
public class PurchaseRequisitionEntity extends BaseBillEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private String subject;
    private Long applyOrgId;
    private Long applicantId;
    private LocalDate requiredDate;
    private String reason;
    @Version
    private Integer version;
}
