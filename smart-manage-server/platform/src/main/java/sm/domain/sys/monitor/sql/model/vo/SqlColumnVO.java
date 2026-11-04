package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SQL 查询结果列定义。
 */
@Data
@AllArgsConstructor
@Schema(description = "SQL 查询结果列")
public class SqlColumnVO {
    @Schema(description = "结果行中对应列值的字段键")
    private String key;
    @Schema(description = "查询结果列标签")
    private String label;
    @Schema(description = "数据库列类型名称")
    private String typeName;
    @Schema(description = "数据库对象注释")
    private String comment;
}
