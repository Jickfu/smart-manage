package sm.domain.sys.base.basicdata.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 基础资料分类持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_basic_data_category")
public class BasicDataCategoryEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属领域ID
     */
    private Long domainId;
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
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 是否系统预置
     */
    private Boolean systemPreset;
    /**
     * 节点编号模式：MANUAL人工、AUTO_LOCKED自动锁定、AUTO_DEFAULT留空自动
     */
    private String numberMode;
    /**
     * 节点编号规则键
     */
    private String numberRuleKey;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
