package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sys_openapi_invocation_log")
public class OpenApiInvocationLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private LocalDateTime requestTime;
    private Long applicationId;
    private String applicationNumber;
    private String credentialKeyId;
    private String operationKey;
    private String requestId;
    private String traceId;
    private String clientIp;
    private String resultType;
    private Integer resultCode;
    private Long durationMs;
    private Integer requestBytes;
    private Integer responseBytes;
    private String errorMessage;
}
