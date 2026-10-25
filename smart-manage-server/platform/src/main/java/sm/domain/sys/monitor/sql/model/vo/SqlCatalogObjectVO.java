package sm.domain.sys.monitor.sql.model.vo;

import lombok.Data;

@Data
public class SqlCatalogObjectVO {
    private String schemaName;
    private String objectName;
    private String objectType;
    private String comment;
}
