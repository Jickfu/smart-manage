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
@TableName("t_sys_openapi_application")
public class OpenApiApplicationEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String number;
    private String name;
    private Boolean enabled;
    private Long proxyUserId;
    private Long proxyOrgId;
    private String authenticationType;
    private String encryptionAlgorithm;
    private String ipPolicyMode;
    private String ipRanges;
    private String description;
    @Version
    private Integer version;
}
