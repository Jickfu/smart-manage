package sm.domain.sys.base.numberrule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Schema(description = "编号引用输出模型")
@Data
public class NumberReferenceVO {
    @Schema(description = "稳定编号引用键")
    private String referenceKey;
    @Schema(description = "名称")
    private String name;
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
    @Schema(description = "该编号引用允许的流水作用域")
    private Set<String> allowedScopes;
    @Schema(description = "编号引用允许使用的受控变量列表")
    private List<NumberVariableVO> variables;
}
