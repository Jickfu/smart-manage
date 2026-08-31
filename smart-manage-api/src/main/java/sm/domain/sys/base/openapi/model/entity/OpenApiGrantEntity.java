package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_grant")
public class OpenApiGrantEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long applicationId;
    private String operationKey;
}
