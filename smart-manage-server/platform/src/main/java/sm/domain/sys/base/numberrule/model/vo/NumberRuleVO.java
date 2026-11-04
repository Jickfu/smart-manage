package sm.domain.sys.base.numberrule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "编号规则输出模型")
@Data
public class NumberRuleVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "稳定规则键")
    private String ruleKey;
    @Schema(description = "稳定编号引用键")
    private String referenceKey;
    @Schema(description = "编号引用名称")
    private String referenceName;
    @Schema(description = "所属功能ID")
    private Long featureId;
    @Schema(description = "稳定功能键")
    private String featureKey;
    @Schema(description = "所属功能名称")
    private String featureName;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属应用名称")
    private String appName;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属领域名称")
    private String domainName;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "编号模板")
    private String pattern;
    @Schema(description = "流水作用域：GLOBAL 全局、ORG 组织、CATEGORY 分类；必须属于编号引用允许范围")
    private String scopeType;
    @Schema(description = "流水重置周期：NEVER、YEAR、MONTH、DAY")
    private String resetPeriod;
    @Schema(description = "新分段起始流水值")
    private Integer startValue;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "是否系统预置")
    private Boolean systemPreset;
    @Schema(description = "是否为该编号引用的默认规则")
    private Boolean defaultRule;
    @Schema(description = "默认编号引用数与非 MANUAL 基础资料分类引用数之和，不是累计生成的编号数量")
    private Long usageCount;
    @Schema(description = "按顺序排列的编号格式段")
    private List<NumberRuleSegmentVO> segments;
    @Schema(description = "说明")
    private String description;
}
