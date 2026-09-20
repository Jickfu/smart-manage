package sm.system.query;

import org.springframework.util.StringUtils;
import sm.system.exception.BizException;
import sm.system.form.PageForm;
import sm.system.response.ResultEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 联表列表使用的安全动态查询描述。
 *
 * <p>SQL 列名只允许来自 Service 声明的常量白名单，前端字段名不会直接拼接到 SQL。</p>
 */
public record ListSqlQuery(List<Condition> conditions, String sortColumn, String sortOrder) {
    public record Field(String column, ListQueryUtil.ValueType valueType,
                        Set<ListQueryUtil.Operator> operators, boolean sortable) {
    }

    public record Condition(String column, ListQueryUtil.Operator operator,
                            Object value, List<Object> values, Object begin, Object endExclusive) {
    }

    public static Field string(String column, boolean sortable) {
        return field(column, ListQueryUtil.ValueType.STRING, sortable,
                ListQueryUtil.Operator.CONTAINS, ListQueryUtil.Operator.NOT_CONTAINS,
                ListQueryUtil.Operator.EQ, ListQueryUtil.Operator.NE,
                ListQueryUtil.Operator.STARTS_WITH, ListQueryUtil.Operator.ENDS_WITH,
                ListQueryUtil.Operator.EMPTY, ListQueryUtil.Operator.NOT_EMPTY);
    }

    public static Field number(String column, boolean sortable) {
        return field(column, ListQueryUtil.ValueType.NUMBER, sortable,
                ListQueryUtil.Operator.EQ, ListQueryUtil.Operator.NE, ListQueryUtil.Operator.GT,
                ListQueryUtil.Operator.GE, ListQueryUtil.Operator.LT, ListQueryUtil.Operator.LE);
    }

    public static Field enumeration(String column, boolean sortable) {
        return field(column, ListQueryUtil.ValueType.ENUM, sortable, ListQueryUtil.Operator.IN);
    }

    public static Field bool(String column, boolean sortable) {
        return field(column, ListQueryUtil.ValueType.BOOLEAN, sortable, ListQueryUtil.Operator.IN);
    }

    public static Field date(String column, boolean sortable) {
        return dateField(column, ListQueryUtil.ValueType.DATE, sortable);
    }

    public static Field dateTime(String column, boolean sortable) {
        return dateField(column, ListQueryUtil.ValueType.DATE_TIME, sortable);
    }

    public static ListSqlQuery of(PageForm form, Map<String, Field> fields) {
        List<Condition> conditions = ListQueryUtil.parseFilters(form.getFilters()).stream()
                .map(condition -> normalize(condition, requireField(condition, fields)))
                .toList();
        if (!StringUtils.hasText(form.getSortField())) {
            return new ListSqlQuery(conditions, null, null);
        }
        Field sortField = fields.get(form.getSortField());
        if (sortField == null || !sortField.sortable()) {
            throw error("不支持排序字段：" + form.getSortField());
        }
        String order = form.getSortOrder();
        if (!"ASC".equalsIgnoreCase(order) && !"DESC".equalsIgnoreCase(order)) {
            throw error("排序方向必须为 ASC 或 DESC");
        }
        return new ListSqlQuery(conditions, sortField.column(), order.toUpperCase());
    }

    private static Field requireField(ListQueryUtil.Condition condition, Map<String, Field> fields) {
        if (condition == null || !StringUtils.hasText(condition.field()) || condition.operator() == null) {
            throw error("筛选字段和操作符不能为空");
        }
        Field field = fields.get(condition.field());
        if (field == null) throw error("不支持筛选字段：" + condition.field());
        if (!field.operators().contains(condition.operator())) {
            throw error("字段不支持该筛选操作：" + condition.field());
        }
        return field;
    }

    private static Condition normalize(ListQueryUtil.Condition condition, Field field) {
        if (condition.operator() == ListQueryUtil.Operator.IN) {
            if (condition.values() == null || condition.values().isEmpty()) throw error("多选筛选值不能为空");
            return new Condition(field.column(), condition.operator(), null,
                    condition.values().stream().map(value -> scalar(field.valueType(), value)).toList(), null, null);
        }
        if (field.valueType() == ListQueryUtil.ValueType.DATE
                || field.valueType() == ListQueryUtil.ValueType.DATE_TIME) {
            return normalizeDate(condition, field);
        }
        if (condition.operator() == ListQueryUtil.Operator.EMPTY
                || condition.operator() == ListQueryUtil.Operator.NOT_EMPTY) {
            return new Condition(field.column(), condition.operator(), null, List.of(), null, null);
        }
        return new Condition(field.column(), condition.operator(), scalar(field.valueType(), condition.value()),
                List.of(), null, null);
    }

    private static Condition normalizeDate(ListQueryUtil.Condition condition, Field field) {
        LocalDate today = LocalDate.now();
        LocalDate begin;
        LocalDate end;
        switch (condition.operator()) {
            case EQ, TODAY -> { begin = condition.operator() == ListQueryUtil.Operator.EQ
                    ? parseDate(condition.value()) : today; end = begin.plusDays(1); }
            case THIS_WEEK -> { begin = today.with(java.time.DayOfWeek.MONDAY); end = begin.plusWeeks(1); }
            case THIS_MONTH -> { begin = today.withDayOfMonth(1); end = begin.plusMonths(1); }
            case LAST_MONTH -> { begin = today.withDayOfMonth(1).minusMonths(1); end = begin.plusMonths(1); }
            case PAST_MONTH -> { begin = today.minusMonths(1); end = today.plusDays(1); }
            case PAST_THREE_MONTHS -> { begin = today.minusMonths(3); end = today.plusDays(1); }
            case BETWEEN -> {
                if (condition.values() == null || condition.values().size() != 2) {
                    throw error("日期范围必须包含开始和结束日期");
                }
                begin = parseDate(condition.values().get(0));
                LocalDate inclusiveEnd = parseDate(condition.values().get(1));
                if (inclusiveEnd.isBefore(begin)) throw error("结束日期不能早于开始日期");
                end = inclusiveEnd.plusDays(1);
            }
            default -> throw error("无效日期操作");
        }
        Object normalizedBegin = field.valueType() == ListQueryUtil.ValueType.DATE
                ? begin : LocalDateTime.of(begin, LocalTime.MIN);
        Object normalizedEnd = field.valueType() == ListQueryUtil.ValueType.DATE
                ? end : LocalDateTime.of(end, LocalTime.MIN);
        return new Condition(field.column(), ListQueryUtil.Operator.BETWEEN, null, List.of(),
                normalizedBegin, normalizedEnd);
    }

    private static Object scalar(ListQueryUtil.ValueType type, Object raw) {
        if (raw == null || !StringUtils.hasText(String.valueOf(raw))) throw error("筛选值不能为空");
        String text = String.valueOf(raw).trim();
        try {
            return switch (type) {
                case STRING, ENUM -> text;
                case NUMBER -> new BigDecimal(text);
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) {
                        throw error("布尔筛选值必须为 true 或 false");
                    }
                    yield Boolean.valueOf(text);
                }
                case DATE -> LocalDate.parse(text);
                case DATE_TIME -> LocalDateTime.parse(text);
            };
        } catch (NumberFormatException | java.time.format.DateTimeParseException exception) {
            throw error("筛选值格式错误");
        }
    }

    private static LocalDate parseDate(Object raw) {
        if (raw == null) throw error("日期筛选值不能为空");
        try { return LocalDate.parse(String.valueOf(raw)); }
        catch (java.time.format.DateTimeParseException exception) { throw error("日期筛选值格式错误"); }
    }

    private static Field dateField(String column, ListQueryUtil.ValueType type, boolean sortable) {
        return field(column, type, sortable, ListQueryUtil.Operator.EQ, ListQueryUtil.Operator.BETWEEN,
                ListQueryUtil.Operator.TODAY, ListQueryUtil.Operator.THIS_WEEK, ListQueryUtil.Operator.THIS_MONTH,
                ListQueryUtil.Operator.LAST_MONTH, ListQueryUtil.Operator.PAST_MONTH,
                ListQueryUtil.Operator.PAST_THREE_MONTHS);
    }

    private static Field field(String column, ListQueryUtil.ValueType type, boolean sortable,
                               ListQueryUtil.Operator... operators) {
        return new Field(column, type, Set.of(operators), sortable);
    }

    private static BizException error(String message) {
        return new BizException(ResultEnum.PARAM_ERROR, message);
    }
}
