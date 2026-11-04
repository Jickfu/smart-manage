package sm.domain.sys.base.numberrule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 编号规则持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_number_rule")
public class NumberRuleEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 稳定规则键
     */
    private String ruleKey;
    /**
     * 稳定编号引用键
     */
    private String referenceKey;
    /**
     * 名称
     */
    private String name;
    /**
     * 编号模板
     */
    private String pattern;
    /**
     * 流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围
     */
    private String scopeType;
    /**
     * 流水重置周期：NEVER、YEAR、MONTH、DAY
     */
    private String resetPeriod;
    /**
     * 新分段起始流水值
     */
    private Long startValue;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 是否系统预置
     */
    private Boolean systemPreset;
    /**
     * 说明
     */
    private String description;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
