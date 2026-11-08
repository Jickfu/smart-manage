package sm.domain.sys.base.feature.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 功能关联的只读数据库对象元数据。
 */
@Schema(description = "功能关联数据对象输出模型")
@Data
public class FeatureDataObjectVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "数据库模式名")
    private String schemaName;
    @Schema(description = "数据对象名")
    private String objectName;
    @Schema(description = "数据库对象类型：TABLE、PARTITIONED_TABLE、VIEW、MATERIALIZED_VIEW、FOREIGN_TABLE")
    private String objectType;
    @Schema(description = "功能关联对象角色：PRIMARY 主对象、DETAIL 明细、RELATION 关联、HISTORY 历史、LOG 日志")
    private String objectRole;
    @Schema(description = "显示名称")
    private String displayName;
    @Schema(description = "排序号")
    private Integer seq;
}
