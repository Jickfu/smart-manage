package sm.domain.sys.monitor.sql.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SqlCatalogResultVO {
    private List<SqlCatalogObjectVO> objects;
    private boolean truncated;
}
