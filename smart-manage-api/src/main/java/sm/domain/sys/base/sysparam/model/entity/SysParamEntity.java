package sm.domain.sys.base.sysparam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 系统参数实体
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_param")
public class SysParamEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 参数编码 */
    private String number;

    /** 参数名称 */
    private String name;

    /** 参数值 */
    private String value;

    /** 描述 */
    private String description;

    /** 是否系统内置 */
    private Boolean isSystem;

    /** 所属应用 ID */
    private Long appId;

    @Version
    private Integer version;
}
