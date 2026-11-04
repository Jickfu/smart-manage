package sm.domain.sys.base.numberrule.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 编号格式段持久化实体。
 */
@Data
@TableName("t_sys_number_rule_segment")
public class NumberRuleSegmentEntity {
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
     * 排序号
     */
    private Integer sort;
    /**
     * 格式段类型：FIXED 固定文本、VARIABLE 受控变量、DATE 日期、SEQUENCE 流水
     */
    private String segmentType;
    /**
     * FIXED 段为固定文本，VARIABLE、DATE 段为编号引用允许的对应类型变量键（内置日期变量为 system.date）；SEQUENCE 段不使用
     */
    private String value;
    /**
     * 日期格式，DATE 段仅支持 yyyy、yyyyMM、yyyyMMdd
     */
    private String format;
    /**
     * 流水位数，SEQUENCE 段取值 1～18
     */
    private Integer length;
    /**
     * 段后分隔符
     */
    private String separator;
}
