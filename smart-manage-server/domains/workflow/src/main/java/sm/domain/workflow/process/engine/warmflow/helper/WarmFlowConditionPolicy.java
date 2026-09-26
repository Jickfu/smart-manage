package sm.domain.workflow.process.engine.warmflow.helper;

import org.dromara.warm.flow.core.dto.SkipJson;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.math.BigDecimal;
import java.util.*;

/** 只分析已通过白名单的 Warm-Flow 单值比较语义，不执行脚本、不引入第二套流程表达式。 */
final class WarmFlowConditionPolicy {
    private WarmFlowConditionPolicy() { }
    static void validate(String expression, FieldType type) {
        var condition = parse(expression);
        if (type == FieldType.NUMBER) {
            try { new BigDecimal(condition.literal()); }
            catch (NumberFormatException failure) { fail("数值字段的比较值必须是有效数字"); }
        } else {
            if (!Set.of("eq", "ne").contains(condition.operator())) fail("文本字段只支持等于或不等于");
            // 上游把可解析数字的字符串按数值比较；文本字段禁止这个歧义，避免运行时转换异常。
            if (org.dromara.warm.flow.core.utils.MathUtil.isNumeric(condition.literal())) fail("文本字段不能使用数值比较常量");
        }
    }

    static void requireDisjoint(List<SkipJson> skips) {
        var conditions = skips.stream().map(SkipJson::getSkipCondition).filter(value -> value != null && !value.isBlank())
                .map(WarmFlowConditionPolicy::parse).toList();
        for (int leftIndex = 0; leftIndex < conditions.size(); leftIndex++) {
            for (int rightIndex = leftIndex + 1; rightIndex < conditions.size(); rightIndex++) {
                if (overlaps(conditions.get(leftIndex), conditions.get(rightIndex))) fail("条件分支可能同时满足，请改为互斥条件；默认分支无需配置条件");
            }
        }
    }

    private static boolean overlaps(Condition left, Condition right) {
        // 两个独立字段可分别满足各自条件，不能证明互斥。
        if (!left.field().equals(right.field())) return true;
        BigDecimal leftNumber = numeric(left.literal());
        BigDecimal rightNumber = numeric(right.literal());
        if (leftNumber != null && rightNumber != null) {
            // 单值比较只在两端点处改变真假；检查端点、两端外侧和中点即可覆盖整个数轴。
            BigDecimal minimum = leftNumber.min(rightNumber);
            BigDecimal maximum = leftNumber.max(rightNumber);
            var probes = List.of(minimum.subtract(BigDecimal.ONE), minimum, minimum.add(maximum).divide(BigDecimal.valueOf(2)), maximum, maximum.add(BigDecimal.ONE));
            return probes.stream().anyMatch(value -> matches(left, value) && matches(right, value));
        }
        if (left.operator().equals("eq")) return right.operator().equals("eq") ? left.literal().equals(right.literal()) : !left.literal().equals(right.literal());
        if (right.operator().equals("eq")) return !left.literal().equals(right.literal());
        return true;
    }

    private static boolean matches(Condition condition, BigDecimal value) {
        int comparison = value.compareTo(new BigDecimal(condition.literal()));
        return switch (condition.operator()) {
            case "eq" -> comparison == 0;
            case "ne" -> comparison != 0;
            case "gt" -> comparison > 0;
            case "ge" -> comparison >= 0;
            case "lt" -> comparison < 0;
            case "le" -> comparison <= 0;
            default -> throw new IllegalStateException("条件未通过白名单");
        };
    }
    private static BigDecimal numeric(String value) { try { return new BigDecimal(value); } catch (NumberFormatException failure) { return null; } }
    private static Condition parse(String expression) {
        int separator = expression.indexOf('|');
        return new Condition(expression.substring(0, 2), expression.substring(4, separator), expression.substring(separator + 1));
    }
    private record Condition(String operator, String field, String literal) { }
    private static void fail(String message) { throw new BizException(ResultEnum.PARAM_ERROR, message); }
}
