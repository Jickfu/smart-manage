package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "数据库对象目录详情输出模型")
@Data
public class SqlCatalogDetailVO {
    @Schema(description = "数据库模式名")
    private String schemaName;
    @Schema(description = "数据对象名")
    private String objectName;
    @Schema(description = "数据库对象类型：TABLE、PARTITIONED_TABLE、VIEW、MATERIALIZED_VIEW、FOREIGN_TABLE")
    private String objectType;
    @Schema(description = "数据库对象注释")
    private String comment;
    @Schema(description = "数据库对象的列元数据列表")
    private List<SqlCatalogColumnVO> columns;
}
