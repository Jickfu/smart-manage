package sm.domain.sys.base.numberrule.service;

import sm.domain.sys.base.numberrule.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.NumberSegmentType;
import sm.domain.sys.base.numberrule.model.NumberVariableDefinition;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class NumberSegmentRenderer {
    private static final int MAX_NUMBER_LENGTH = 64;
    private static final Set<String> DATE_FORMATS = Set.of("yyyy", "yyyyMM", "yyyyMMdd");

    private NumberSegmentRenderer() {
    }

    static void validate(List<NumberRuleSegmentEntity> segments, NumberReferenceDefinition reference) {
        if (segments == null || segments.isEmpty()) throw invalid("编号格式段不能为空");
        if (segments.size() > 20) throw invalid("编号格式段不能超过20段");
        Map<String, NumberVariableDefinition> variables = reference.variables().stream()
                .collect(java.util.stream.Collectors.toMap(NumberVariableDefinition::key, value -> value));
        variables.put(NumberRuleBuiltInVariables.SYSTEM_DATE_KEY, NumberRuleBuiltInVariables.SYSTEM_DATE);
        int sequenceCount = 0;
        int expectedSort = 1;
        int minimumResultLength = 0;
        for (NumberRuleSegmentEntity segment : sorted(segments)) {
            if (!Integer.valueOf(expectedSort).equals(segment.getSort())) throw invalid("编号格式段顺序必须连续");
            expectedSort++;
            NumberSegmentType type = parseType(segment.getSegmentType());
            String separator = segment.getSeparator() == null ? "" : segment.getSeparator();
            if (separator.length() > 10) throw invalid("段间分隔符不能超过10个字符");
            minimumResultLength += separator.length();
            switch (type) {
                case FIXED -> {
                    if (segment.getValue() == null || segment.getValue().isBlank()) throw invalid("固定值不能为空");
                    minimumResultLength += segment.getValue().length();
                }
                case VARIABLE, DATE -> {
                    NumberVariableDefinition variable = variables.get(segment.getValue());
                    if (variable == null || variable.segmentType() != type) {
                        throw invalid("编号引用不支持变量：" + segment.getValue());
                    }
                    if (type == NumberSegmentType.DATE && !DATE_FORMATS.contains(segment.getFormat())) {
                        throw invalid("日期格式只支持 yyyy、yyyyMM 或 yyyyMMdd");
                    }
                    if (type == NumberSegmentType.DATE) minimumResultLength += segment.getFormat().length();
                }
                case SEQUENCE -> {
                    if (segment.getLength() == null || segment.getLength() < 1 || segment.getLength() > 18) {
                        throw invalid("流水号位数必须在1到18之间");
                    }
                    minimumResultLength += segment.getLength();
                    sequenceCount++;
                }
            }
        }
        if (sequenceCount != 1) throw invalid("编号格式必须且只能包含一个顺序号段");
        if (minimumResultLength > MAX_NUMBER_LENGTH) {
            throw invalid("编号固定内容已超过64个字符，请调整格式段");
        }
    }

    static String render(List<NumberRuleSegmentEntity> segments, long sequenceValue,
                         NumberGenerationContext context, NumberVariableResolverRegistry resolvers) {
        StringBuilder result = new StringBuilder();
        for (NumberRuleSegmentEntity segment : sorted(segments)) {
            NumberSegmentType type = parseType(segment.getSegmentType());
            switch (type) {
                case FIXED -> result.append(segment.getValue());
                case VARIABLE -> result.append(resolvers.resolve(segment.getValue(), context));
                case DATE -> result.append(formatDate(resolveDate(segment.getValue(), context), segment.getFormat()));
                case SEQUENCE -> result.append(String.format("%0" + segment.getLength() + "d", sequenceValue));
            }
            result.append(segment.getSeparator() == null ? "" : segment.getSeparator());
        }
        if (result.length() > MAX_NUMBER_LENGTH) throw invalid("生成的编号超过64个字符，请调整格式段");
        return result.toString();
    }

    static String preview(List<NumberRuleSegmentEntity> segments, long sequenceValue) {
        StringBuilder result = new StringBuilder();
        LocalDate today = LocalDate.now();
        for (NumberRuleSegmentEntity segment : sorted(segments)) {
            NumberSegmentType type = parseType(segment.getSegmentType());
            switch (type) {
                case FIXED -> result.append(segment.getValue());
                case VARIABLE -> result.append(sampleValue(segment.getValue()));
                case DATE -> result.append(formatDate(today, segment.getFormat()));
                case SEQUENCE -> result.append(String.format("%0" + segment.getLength() + "d", sequenceValue));
            }
            result.append(segment.getSeparator() == null ? "" : segment.getSeparator());
        }
        if (result.length() > MAX_NUMBER_LENGTH) throw invalid("生成的编号超过64个字符，请调整格式段");
        return result.toString();
    }

    static String compilePattern(List<NumberRuleSegmentEntity> segments) {
        StringBuilder pattern = new StringBuilder();
        for (NumberRuleSegmentEntity segment : sorted(segments)) {
            switch (parseType(segment.getSegmentType())) {
                case FIXED -> pattern.append(segment.getValue());
                case VARIABLE -> pattern.append('{').append(segment.getValue()).append('}');
                case DATE -> pattern.append('{').append(segment.getValue()).append(':')
                        .append(segment.getFormat()).append('}');
                case SEQUENCE -> pattern.append("{seq:").append(segment.getLength()).append('}');
            }
            pattern.append(segment.getSeparator() == null ? "" : segment.getSeparator());
        }
        return pattern.toString();
    }

    private static String sampleValue(String variableKey) {
        return switch (variableKey) {
            case "org.number" -> "ORG";
            case "category.number" -> "CATEGORY";
            default -> "VALUE";
        };
    }

    private static LocalDate resolveDate(String variableKey, NumberGenerationContext context) {
        return NumberRuleBuiltInVariables.SYSTEM_DATE_KEY.equals(variableKey)
                ? LocalDate.now()
                : context.businessDate();
    }

    private static String formatDate(LocalDate date, String format) {
        if (date == null) throw invalid("编号格式需要业务日期");
        if (!DATE_FORMATS.contains(format)) throw invalid("日期格式无效");
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    private static List<NumberRuleSegmentEntity> sorted(List<NumberRuleSegmentEntity> segments) {
        return segments.stream().sorted(Comparator.comparing(NumberRuleSegmentEntity::getSort)).toList();
    }

    private static NumberSegmentType parseType(String value) {
        try {
            return NumberSegmentType.valueOf(value);
        } catch (RuntimeException exception) {
            throw invalid("编号格式段类型无效");
        }
    }

    private static BizException invalid(String message) {
        return new BizException(ResultEnum.PARAM_ERROR, message);
    }
}
