package sm.domain.sys.base.feature.model.vo;

import lombok.Data;

/** 功能关联的只读数据库对象元数据。 */
@Data
public class FeatureDataObjectVO {
    private Long id;
    private String schemaName;
    private String objectName;
    private String objectType;
    private String objectRole;
    private String displayName;
    private Integer seq;
}
