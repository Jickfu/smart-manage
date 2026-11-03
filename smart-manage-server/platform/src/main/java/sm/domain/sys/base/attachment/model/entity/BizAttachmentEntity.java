package sm.domain.sys.base.attachment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 业务单据-附件映射实体
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_biz_attachment")
public class BizAttachmentEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务类型
     */
    private String bizType;
    /**
     * 业务ID
     */
    private String bizId;
    /**
     * 附件ID
     */
    private Long attachmentId;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;
}
