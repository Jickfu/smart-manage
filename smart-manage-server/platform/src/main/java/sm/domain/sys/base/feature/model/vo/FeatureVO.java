package sm.domain.sys.base.feature.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "功能输出模型")
@Data
public class FeatureVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "稳定功能键")
    private String featureKey;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属应用名称")
    private String appName;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属领域名称")
    private String domainName;
    @Schema(description = "系统默认名称")
    private String defaultName;
    @Schema(description = "管理员自定义名称")
    private String customName;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "系统默认排序")
    private Integer defaultSeq;
    @Schema(description = "管理员自定义排序")
    private Integer customSeq;
    @Schema(description = "排序号")
    private Integer seq;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "是否在功能目录中可见")
    private Boolean visible;
    @Schema(description = "功能来源：SYSTEM 系统、PLUGIN 插件、EXTERNAL 外部")
    private String source;
}
