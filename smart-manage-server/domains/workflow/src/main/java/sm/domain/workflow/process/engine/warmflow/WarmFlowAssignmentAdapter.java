package sm.domain.workflow.process.engine.warmflow;

import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.strategy.HandlerStrategy;
import org.dromara.warm.flow.core.utils.ExpressionUtil;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import sm.domain.workflow.process.assignment.service.AssignmentService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Map;

/** 使用上游表达式扩展点，不启用 Spring Bean/脚本表达式。 */
@Component
@RequiredArgsConstructor
public class WarmFlowAssignmentAdapter implements HandlerStrategy, SmartInitializingSingleton {
    private final AssignmentService assignments;

    @Override public String getType() { return "sm:"; }

    @Override
    public Object preEval(String expression, Map<String, Object> variables) {
        Object organization = variables.get("orgId");
        if (!(organization instanceof Number value) || value.longValue() <= 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程缺少服务端单据组织");
        }
        return assignments.resolve(expression, value.longValue());
    }

    @Override
    public void afterSingletonsInstantiated() {
        ExpressionUtil.setExpression(this);
    }
}
