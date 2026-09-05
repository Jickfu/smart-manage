package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_release")
public class OpenApiReleaseEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String apiNumber;
    private String apiVersion;
    private String operationKey;
    private String name;
    private String httpMethod;
    private String path;
    private String domainKey;
    private String domainName;
    private String applicationKey;
    private String applicationName;
    private String featureKey;
    private String featureName;
    private String status;
    private String description;
    private String requestSchema;
    private String responseSchema;
    private String documentation;
    private String requestExample;
    private String responseExample;
    private Boolean systemPreset;
    @Version
    private Integer version;
}
