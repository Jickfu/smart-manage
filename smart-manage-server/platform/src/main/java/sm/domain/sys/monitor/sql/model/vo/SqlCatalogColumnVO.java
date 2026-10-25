package sm.domain.sys.monitor.sql.model.vo;

import lombok.Data;

@Data
public class SqlCatalogColumnVO {
    private Integer position;
    private String columnName;
    private String dataType;
    private Boolean nullable;
    private Boolean primaryKey;
    private String defaultValue;
    private String comment;
}
