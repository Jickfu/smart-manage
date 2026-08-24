package sm.domain.sys.base.numberrule.contract.model;

/** 可由编号格式使用的受控业务变量。 */
public record NumberVariableDefinition(String key, String name, NumberSegmentType segmentType) {
}
