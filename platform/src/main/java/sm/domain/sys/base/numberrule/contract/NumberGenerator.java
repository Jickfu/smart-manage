package sm.domain.sys.base.numberrule.contract;

import sm.domain.sys.base.numberrule.contract.model.NumberGenerationContext;

/** 供业务领域按稳定引用键生成正式业务编号的契约。 */
public interface NumberGenerator {

    String nextNumber(String referenceKey, NumberGenerationContext context);
}
