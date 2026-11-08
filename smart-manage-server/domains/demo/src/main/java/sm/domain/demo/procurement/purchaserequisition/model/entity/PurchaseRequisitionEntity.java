package sm.domain.demo.procurement.purchaserequisition.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseBillEntity;

import java.time.LocalDate;

/**
 * 采购申请聚合根。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demo_purchase_requisition")
public class PurchaseRequisitionEntity extends BaseBillEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 主题
     */
    private String subject;
    /**
     * 申请人ID
     */
    private Long applicantId;
    /**
     * 需求日期
     */
    private LocalDate requiredDate;
    /**
     * 申请原因
     */
    private String reason;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
