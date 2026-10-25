package sm.domain.sys.monitor.sql.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SqlCatalogDetailVO {
    private String schemaName;
    private String objectName;
    private String objectType;
    private String comment;
    private List<SqlCatalogColumnVO> columns;
}
