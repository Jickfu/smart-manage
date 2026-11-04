package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开放接口调用日志持久化实体。
 */
@Data
@TableName("t_sys_openapi_invocation_log")
public class OpenApiInvocationLogEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 请求时间
     */
    private LocalDateTime requestTime;
    /**
     * 接入应用 ID
     */
    private Long applicationId;
    /**
     * 接入应用编码快照
     */
    private String applicationNumber;
    /**
     * 本次请求使用的凭据标识
     */
    private String credentialKeyId;
    /**
     * API 操作的稳定标识
     */
    private String operationKey;
    /**
     * 请求唯一标识
     */
    private String requestId;
    /**
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 请求客户端 IP
     */
    private String clientIp;
    /**
     * API 调用结果：SUCCESS、BUSINESS_FAILED、AUTHENTICATION_FAILED、ACCESS_DENIED、SYSTEM_FAILED
     */
    private String resultType;
    /**
     * 调用结果业务码
     */
    private Integer resultCode;
    /**
     * 耗时(ms)
     */
    private Long durationMs;
    /**
     * 请求正文长度，单位字节
     */
    private Integer requestBytes;
    /**
     * 响应正文长度，单位字节
     */
    private Integer responseBytes;
    /**
     * 错误说明
     */
    private String errorMessage;
}
