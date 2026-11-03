package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 开放接口授权持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_grant")
public class OpenApiGrantEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 接入应用 ID
     */
    private Long applicationId;
    /**
     * API 操作的稳定标识
     */
    private String operationKey;
}
