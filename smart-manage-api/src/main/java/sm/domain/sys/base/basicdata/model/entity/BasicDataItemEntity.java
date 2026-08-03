package sm.domain.sys.base.basicdata.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_basic_data_item")
public class BasicDataItemEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long categoryId;
    private Long parentId;
    private String number;
    private String name;
    private String remark;
    private Integer sort;
    private Boolean enabled;
    private Boolean systemPreset;
    private Integer level;
    private String numberPath;
    private String namePath;
    @TableField("is_leaf")
    private Boolean isLeaf;
    @Version
    private Integer version;
}
