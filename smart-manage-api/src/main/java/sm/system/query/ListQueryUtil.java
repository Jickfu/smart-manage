package sm.system.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import sm.system.exception.BizException;
import sm.system.form.PageForm;
import sm.system.response.ResultEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用列表查询工具。
 *
 * <p>客户端字段键不会直接进入 SQL。每个领域 Service 必须显式提供字段白名单和方法引用，
 * 本工具只负责校验操作符、转换值并应用条件。</p>
 */
public final class ListQueryUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_FILTER_JSON_LENGTH = 20_000;
    private static final int MAX_FILTER_COUNT = 20;

    private ListQueryUtil() {
    }

    public enum ValueType {
        STRING,
        NUMBER,
        ENUM,
        BOOLEAN,
        DATE,
        DATE_TIME
    }

    public enum Operator {
        CONTAINS,
        NOT_CONTAINS,
        EQ,
        NE,
        STARTS_WITH,
        ENDS_WITH,
        EMPTY,
        NOT_EMPTY,
        GT,
        GE,
        LT,
        LE,
        IN,
        TODAY,
        THIS_WEEK,
        THIS_MONTH,
        LAST_MONTH,
        PAST_MONTH,
        PAST_THREE_MONTHS,
        BETWEEN
    }

    public record Field<T>(
            SFunction<T, ?> column,
            ValueType valueType,
            Set<Operator> operators,
            boolean sortable) {
    }

    public static <T> Field<T> string(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.STRING,
                Set.of(Operator.CONTAINS, Operator.NOT_CONTAINS, Operator.EQ, Operator.NE,
                        Operator.STARTS_WITH, Operator.ENDS_WITH, Operator.EMPTY, Operator.NOT_EMPTY),
                sortable);
    }

    public static <T> Field<T> number(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.NUMBER,
                Set.of(Operator.EQ, Operator.NE, Operator.GT, Operator.GE, Operator.LT, Operator.LE),
                sortable);
    }

    public static <T> Field<T> enumeration(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.ENUM, Set.of(Operator.IN), sortable);
    }

    public static <T> Field<T> bool(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.BOOLEAN, Set.of(Operator.IN), sortable);
    }

    public static <T> Field<T> date(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.DATE,
                Set.of(Operator.EQ, Operator.BETWEEN, Operator.TODAY, Operator.THIS_WEEK,
                        Operator.THIS_MONTH, Operator.LAST_MONTH, Operator.PAST_MONTH,
                        Operator.PAST_THREE_MONTHS), sortable);
    }

    public static <T> Field<T> dateTime(SFunction<T, ?> column, boolean sortable) {
        return new Field<>(column, ValueType.DATE_TIME,
                Set.of(Operator.EQ, Operator.BETWEEN, Operator.TODAY, Operator.THIS_WEEK,
                        Operator.THIS_MONTH, Operator.LAST_MONTH, Operator.PAST_MONTH,
                        Operator.PAST_THREE_MONTHS), sortable);
    }

    public static <T> void apply(
            LambdaQueryWrapper<T> wrapper,
            PageForm form,
            Map<String, Field<T>> fields) {
        applyFilters(wrapper, parseFilters(form.getFilters()), fields);
        applySort(wrapper, form.getSortField(), form.getSortOrder(), fields);
    }

    public static boolean hasSort(PageForm form) {
        return StringUtils.hasText(form.getSortField());
    }

    public static boolean isSortedBy(PageForm form, String field) {
        return field != null && field.equals(form.getSortField());
    }

    public static List<Condition> parseFilters(String filtersJson) {
        if (!StringUtils.hasText(filtersJson)) {
            return Collections.emptyList();
        }
        if (filtersJson.length() > MAX_FILTER_JSON_LENGTH) {
            throw parameterError("筛选条件过长");
        }
        try {
            List<Condition> conditions = OBJECT_MAPPER.readValue(
                    filtersJson, new TypeReference<List<Condition>>() { });
            if (conditions.size() > MAX_FILTER_COUNT) {
                throw parameterError("筛选条件数量不能超过" + MAX_FILTER_COUNT);
            }
            return conditions;
        } catch (JsonProcessingException exception) {
            throw parameterError("筛选条件格式错误");
        }
    }

    private static <T> void applyFilters(
            LambdaQueryWrapper<T> wrapper,
            List<Condition> conditions,
            Map<String, Field<T>> fields) {
        for (Condition condition : conditions) {
            if (condition == null || !StringUtils.hasText(condition.field()) || condition.operator() == null) {
                throw parameterError("筛选字段和操作符不能为空");
            }
            Field<T> field = fields.get(condition.field());
            if (field == null) {
                throw parameterError("不支持筛选字段：" + condition.field());
            }
            if (!field.operators().contains(condition.operator())) {
                throw parameterError("字段不支持该筛选操作：" + condition.field());
            }
            applyCondition(wrapper, field, condition);
        }
    }

    private static <T> void applyCondition(
            LambdaQueryWrapper<T> wrapper,
            Field<T> field,
            Condition condition) {
        SFunction<T, ?> column = field.column();
        Operator operator = condition.operator();
        switch (operator) {
            case CONTAINS -> wrapper.like(column, requiredText(condition));
            case NOT_CONTAINS -> wrapper.notLike(column, requiredText(condition));
            case STARTS_WITH -> wrapper.likeRight(column, requiredText(condition));
            case ENDS_WITH -> wrapper.likeLeft(column, requiredText(condition));
            case EMPTY -> wrapper.and(nested -> nested.isNull(column).or().eq(column, ""));
            case NOT_EMPTY -> wrapper.isNotNull(column).ne(column, "");
            case IN -> applyIn(wrapper, field, condition);
            case EQ, NE, GT, GE, LT, LE -> applyComparison(wrapper, field, condition);
            case TODAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, PAST_MONTH, PAST_THREE_MONTHS, BETWEEN ->
                    applyDateRange(wrapper, field, condition);
        }
    }

    private static <T> void applyIn(
            LambdaQueryWrapper<T> wrapper,
            Field<T> field,
            Condition condition) {
        if (condition.values() == null || condition.values().isEmpty()) {
            throw parameterError("多选筛选值不能为空");
        }
        List<Object> values = condition.values().stream()
                .map(value -> convertScalar(field.valueType(), value))
                .toList();
        wrapper.in(field.column(), values);
    }

    private static <T> void applyComparison(
            LambdaQueryWrapper<T> wrapper,
            Field<T> field,
            Condition condition) {
        if ((field.valueType() == ValueType.DATE || field.valueType() == ValueType.DATE_TIME)
                && condition.operator() == Operator.EQ) {
            applyEqualDate(wrapper, field, condition);
            return;
        }
        Object value = convertScalar(field.valueType(), condition.value());
        switch (condition.operator()) {
            case EQ -> wrapper.eq(field.column(), value);
            case NE -> wrapper.ne(field.column(), value);
            case GT -> wrapper.gt(field.column(), value);
            case GE -> wrapper.ge(field.column(), value);
            case LT -> wrapper.lt(field.column(), value);
            case LE -> wrapper.le(field.column(), value);
            default -> throw parameterError("无效比较操作");
        }
    }

    private static <T> void applyEqualDate(
            LambdaQueryWrapper<T> wrapper,
            Field<T> field,
            Condition condition) {
        LocalDate date = parseDate(condition.value());
        if (field.valueType() == ValueType.DATE) {
            wrapper.eq(field.column(), date);
        } else {
            wrapper.ge(field.column(), date.atStartOfDay())
                    .lt(field.column(), date.plusDays(1).atStartOfDay());
        }
    }

    private static <T> void applyDateRange(
            LambdaQueryWrapper<T> wrapper,
            Field<T> field,
            Condition condition) {
        LocalDate today = LocalDate.now();
        LocalDate begin;
        LocalDate endExclusive;
        switch (condition.operator()) {
            case TODAY -> {
                begin = today;
                endExclusive = today.plusDays(1);
            }
            case THIS_WEEK -> {
                begin = today.with(java.time.DayOfWeek.MONDAY);
                endExclusive = begin.plusWeeks(1);
            }
            case THIS_MONTH -> {
                begin = today.withDayOfMonth(1);
                endExclusive = begin.plusMonths(1);
            }
            case LAST_MONTH -> {
                begin = today.withDayOfMonth(1).minusMonths(1);
                endExclusive = begin.plusMonths(1);
            }
            case PAST_MONTH -> {
                begin = today.minusMonths(1);
                endExclusive = today.plusDays(1);
            }
            case PAST_THREE_MONTHS -> {
                begin = today.minusMonths(3);
                endExclusive = today.plusDays(1);
            }
            case BETWEEN -> {
                if (condition.values() == null || condition.values().size() != 2) {
                    throw parameterError("日期范围必须包含开始和结束日期");
                }
                begin = parseDate(condition.values().get(0));
                LocalDate inclusiveEnd = parseDate(condition.values().get(1));
                if (inclusiveEnd.isBefore(begin)) {
                    throw parameterError("结束日期不能早于开始日期");
                }
                endExclusive = inclusiveEnd.plusDays(1);
            }
            default -> throw parameterError("无效日期操作");
        }
        if (field.valueType() == ValueType.DATE) {
            wrapper.ge(field.column(), begin).lt(field.column(), endExclusive);
        } else {
            wrapper.ge(field.column(), LocalDateTime.of(begin, LocalTime.MIN))
                    .lt(field.column(), LocalDateTime.of(endExclusive, LocalTime.MIN));
        }
    }

    private static <T> void applySort(
            LambdaQueryWrapper<T> wrapper,
            String sortField,
            String sortOrder,
            Map<String, Field<T>> fields) {
        if (!StringUtils.hasText(sortField)) {
            return;
        }
        Field<T> field = fields.get(sortField);
        if (field == null || !field.sortable()) {
            throw parameterError("不支持排序字段：" + sortField);
        }
        boolean ascending;
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            ascending = true;
        } else if ("DESC".equalsIgnoreCase(sortOrder)) {
            ascending = false;
        } else {
            throw parameterError("排序方向必须为 ASC 或 DESC");
        }
        wrapper.orderBy(true, ascending, field.column());
    }

    private static Object convertScalar(ValueType type, Object rawValue) {
        if (rawValue == null) {
            throw parameterError("筛选值不能为空");
        }
        String text = String.valueOf(rawValue).trim();
        if (!StringUtils.hasText(text)) {
            throw parameterError("筛选值不能为空");
        }
        try {
            return switch (type) {
                case STRING, ENUM -> text;
                case NUMBER -> new BigDecimal(text);
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) {
                        throw parameterError("布尔筛选值必须为 true 或 false");
                    }
                    yield Boolean.valueOf(text);
                }
                case DATE -> LocalDate.parse(text);
                case DATE_TIME -> LocalDateTime.parse(text);
            };
        } catch (NumberFormatException | java.time.format.DateTimeParseException exception) {
            throw parameterError("筛选值格式错误");
        }
    }

    private static String requiredText(Condition condition) {
        if (condition.value() == null || !StringUtils.hasText(String.valueOf(condition.value()))) {
            throw parameterError("文本筛选值不能为空");
        }
        return String.valueOf(condition.value()).trim();
    }

    private static LocalDate parseDate(Object rawValue) {
        if (rawValue == null) {
            throw parameterError("日期筛选值不能为空");
        }
        try {
            return LocalDate.parse(String.valueOf(rawValue));
        } catch (java.time.format.DateTimeParseException exception) {
            throw parameterError("日期筛选值格式错误");
        }
    }

    private static BizException parameterError(String message) {
        return new BizException(ResultEnum.PARAM_ERROR, message);
    }

    public record Condition(
            String field,
            // 前端类型用于渲染控件，使用小写 string/number/date 等值；后端以字段白名单类型为准。
            String type,
            Operator operator,
            Object value,
            List<Object> values) {
    }
}
