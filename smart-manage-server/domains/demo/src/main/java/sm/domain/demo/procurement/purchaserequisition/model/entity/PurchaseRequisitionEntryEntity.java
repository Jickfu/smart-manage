package sm.domain.demo.procurement.purchaserequisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 采购申请明细，生命周期完全从属于采购申请。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demo_purchase_requisition_entry")
public class PurchaseRequisitionEntryEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long parentId;
    private String materialName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private LocalDate requiredDate;
    private String remark;
    private Integer sort;
}
