package sm.domain.demo.procurement.purchaserequisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购申请明细，生命周期完全从属于采购申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demo_purchase_requisition_entry")
public class PurchaseRequisitionEntryEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 父ID
     */
    private Long parentId;
    /**
     * 物料名称
     */
    private String materialName;
    /**
     * 规格型号
     */
    private String specification;
    /**
     * 单位
     */
    private String unit;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 需求日期
     */
    private LocalDate requiredDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 排序号
     */
    private Integer sort;
}
