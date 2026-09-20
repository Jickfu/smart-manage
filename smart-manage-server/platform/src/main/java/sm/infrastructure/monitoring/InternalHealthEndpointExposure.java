package sm.infrastructure.monitoring;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.EndpointExposureOutcomeContributor;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;

/** 保留进程内健康采样所需的 Bean，不开放任何 Web 或 JMX 传输入口。 */
public final class InternalHealthEndpointExposure implements EndpointExposureOutcomeContributor {
    @Override
    public ConditionOutcome getExposureOutcome(EndpointId endpointId, Set<EndpointExposure> exposures,
            ConditionMessage.Builder message) {
        // Boot 的通用可用性检查传入全部技术；显式 WEB/JMX 检查仍服从原始暴露配置。
        if (EndpointId.of("health").equals(endpointId)
                && exposures.equals(EnumSet.allOf(EndpointExposure.class))) {
            return ConditionOutcome.match(message.because("required for in-process health sampling"));
        }
        return null;
    }
}
