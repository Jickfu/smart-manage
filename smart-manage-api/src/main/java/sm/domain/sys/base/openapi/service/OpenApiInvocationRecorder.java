package sm.domain.sys.base.openapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.openapi.mapper.OpenApiInvocationLogMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiInvocationLogEntity;

/** 调用审计写入器；日志故障不覆盖已形成的业务响应。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenApiInvocationRecorder {
    private final OpenApiInvocationLogMapper mapper;

    public void record(RecordCommand command) {
        OpenApiInvocationLogEntity entity = new OpenApiInvocationLogEntity();
        entity.setRequestTime(command.requestTime());
        entity.setApplicationId(command.applicationId());
        entity.setApplicationNumber(command.applicationNumber());
        entity.setCredentialKeyId(command.credentialKeyId());
        entity.setOperationKey(command.operationKey());
        entity.setRequestId(command.requestId());
        entity.setTraceId(command.traceId());
        entity.setClientIp(command.clientIp());
        entity.setResultType(command.resultType());
        entity.setResultCode(command.resultCode());
        entity.setDurationMs(command.durationMs());
        entity.setRequestBytes(command.requestBytes());
        entity.setResponseBytes(command.responseBytes());
        entity.setErrorMessage(limit(command.errorMessage(), 500));
        try {
            mapper.insert(entity);
        } catch (RuntimeException exception) {
            log.error("OpenAPI 调用审计写入失败: requestId={}, operationKey={}",
                    command.requestId(), command.operationKey(), exception);
        }
    }

    private String limit(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record RecordCommand(java.time.LocalDateTime requestTime, Long applicationId,
                                String applicationNumber, String credentialKeyId,
                                String operationKey, String requestId, String traceId,
                                String clientIp, String resultType, Integer resultCode,
                                long durationMs, int requestBytes, int responseBytes,
                                String errorMessage) {
    }
}
