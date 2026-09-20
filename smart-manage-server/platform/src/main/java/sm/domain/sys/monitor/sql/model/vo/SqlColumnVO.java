package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** SQL 查询结果列定义。 */
@Data
@AllArgsConstructor
@Schema(description = "SQL 查询结果列")
public class SqlColumnVO {
    private String key;
    private String label;
    private String typeName;
    private String comment;
}
