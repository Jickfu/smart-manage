package sm.domain.sys.base.basicdata.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_basic_data_category")
public class BasicDataCategoryEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long cloudId;
    private String number;
    private String name;
    private String remark;
    private Boolean enabled;
    private Boolean systemPreset;
    @Version
    private Integer version;
}
