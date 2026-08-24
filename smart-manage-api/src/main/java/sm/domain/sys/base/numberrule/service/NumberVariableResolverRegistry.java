package sm.domain.sys.base.numberrule.service;

import org.springframework.stereotype.Component;
import sm.domain.sys.base.numberrule.contract.model.NumberGenerationContext;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
class NumberVariableResolverRegistry {
    private final Map<String, NumberVariableResolver> resolvers;

    NumberVariableResolverRegistry(List<NumberVariableResolver> resolverList) {
        Map<String, NumberVariableResolver> registered = new LinkedHashMap<>();
        for (NumberVariableResolver resolver : resolverList) {
            if (registered.putIfAbsent(resolver.variableKey(), resolver) != null) {
                throw new IllegalStateException("编号变量重复注册：" + resolver.variableKey());
            }
        }
        this.resolvers = Map.copyOf(registered);
    }

    String resolve(String variableKey, NumberGenerationContext context) {
        NumberVariableResolver resolver = resolvers.get(variableKey);
        if (resolver == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号变量没有解析器：" + variableKey);
        }
        String value = resolver.resolve(context);
        if (value == null || value.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号变量值不能为空：" + variableKey);
        }
        return value.trim();
    }
}
