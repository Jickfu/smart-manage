package sm.domain.sys.base.numberrule.service;

import org.springframework.stereotype.Component;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NumberReferenceRegistry {
    private final Map<String, NumberReferenceDefinition> definitions;

    public NumberReferenceRegistry(List<NumberReferenceProvider> providers) {
        Map<String, NumberReferenceDefinition> registered = new LinkedHashMap<>();
        for (NumberReferenceProvider provider : providers) {
            NumberReferenceDefinition definition = provider.definition();
            if (registered.putIfAbsent(definition.referenceKey(), definition) != null) {
                throw new IllegalStateException("编号引用重复注册：" + definition.referenceKey());
            }
        }
        this.definitions = Map.copyOf(registered);
    }

    public NumberReferenceDefinition require(String referenceKey) {
        NumberReferenceDefinition definition = definitions.get(referenceKey);
        if (definition == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号引用未在业务模块注册：" + referenceKey);
        }
        return definition;
    }
}
