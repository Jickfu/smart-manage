package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "数据库对象目录查询结果")
@Data
@AllArgsConstructor
public class SqlCatalogResultVO {
    @Schema(description = "数据库对象列表")
    private List<SqlCatalogObjectVO> objects;
    @Schema(description = "对象列表是否因结果数量限制被截断")
    private boolean truncated;
}
