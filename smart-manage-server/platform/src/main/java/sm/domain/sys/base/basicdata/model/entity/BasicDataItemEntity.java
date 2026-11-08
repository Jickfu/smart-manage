package sm.domain.sys.base.basicdata.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 基础资料持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_basic_data_item")
public class BasicDataItemEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 基础资料分类ID
     */
    private Long categoryId;
    /**
     * 父ID
     */
    private Long parentId;
    /**
     * 业务编码
     */
    private String number;
    /**
     * 名称
     */
    private String name;
    /**
     * 说明
     */
    private String description;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 是否系统预置
     */
    private Boolean systemPreset;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 长编码
     */
    private String numberPath;
    /**
     * 长名称
     */
    private String namePath;
    /**
     * 是否叶子节点
     */
    @TableField("is_leaf")
    private Boolean isLeaf;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
