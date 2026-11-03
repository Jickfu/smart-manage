package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "数据库对象目录字段输出模型")
@Data
public class SqlCatalogColumnVO {
    @Schema(description = "数据库列在对象中的顺序")
    private Integer position;
    @Schema(description = "数据库列名")
    private String columnName;
    @Schema(description = "数据库列类型")
    private String dataType;
    @Schema(description = "数据库列是否允许为空")
    private Boolean nullable;
    @Schema(description = "数据库列是否属于主键")
    private Boolean primaryKey;
    @Schema(description = "数据库列默认值表达式")
    private String defaultValue;
    @Schema(description = "数据库对象注释")
    private String comment;
}
