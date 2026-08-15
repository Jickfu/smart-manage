package sm.domain.sys.base.numberrule.service;

import sm.domain.sys.base.numberrule.model.NumberGenerationContext;

/** 受控业务变量解析器；禁止把表名、SQL 或任意属性路径交给用户配置。 */
public interface NumberVariableResolver {
    String variableKey();

    String resolve(NumberGenerationContext context);
}
