package sm.domain.sys.base.numberrule.model;

import java.time.LocalDate;

/** 编号生成所需的受控业务上下文，变量值只允许由代码注册的解析器读取。 */
public record NumberGenerationContext(
        Long orgId,
        Long categoryId,
        LocalDate businessDate
) {
    public static NumberGenerationContext forOrganization(Long orgId, LocalDate businessDate) {
        return new NumberGenerationContext(orgId, null, businessDate);
    }

    public static NumberGenerationContext forCategory(Long categoryId) {
        return new NumberGenerationContext(null, categoryId, null);
    }
}
