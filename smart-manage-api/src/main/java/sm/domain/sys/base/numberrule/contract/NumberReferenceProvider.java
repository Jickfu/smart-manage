package sm.domain.sys.base.numberrule.contract;

import sm.domain.sys.base.numberrule.contract.model.NumberReferenceDefinition;

/** 业务模块显式注册自己的编号引用及可用变量。 */
public interface NumberReferenceProvider {
    NumberReferenceDefinition definition();
}
