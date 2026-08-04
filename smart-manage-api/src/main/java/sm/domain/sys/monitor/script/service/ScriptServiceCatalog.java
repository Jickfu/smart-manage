package sm.domain.sys.monitor.script.service;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.script.model.vo.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.time.temporal.Temporal;
import java.util.*;

/** 统一维护脚本可调用 Service 的识别、方法解析和帮助元数据，避免展示能力与执行能力产生偏差。 */
@Component
@RequiredArgsConstructor
class ScriptServiceCatalog {
    private static final Set<Class<? extends Annotation>> REQUIRED_ANNOTATIONS =
            Set.of(NotNull.class, NotBlank.class, NotEmpty.class);

    private final ApplicationContext applicationContext;
    private final JsonMapper jsonMapper;

    ServiceTarget requireService(String beanName) {
        Object bean;
        try {
            bean = applicationContext.getBean(beanName);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("公开业务 Service 不存在：" + beanName);
        }
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (!isAllowedService(targetClass)) {
            throw new IllegalArgumentException("不允许访问该 Bean：" + beanName);
        }
        return new ServiceTarget(bean, targetClass);
    }

    boolean isAllowedService(Class<?> targetClass) {
        String className = targetClass.getName();
        return Modifier.isPublic(targetClass.getModifiers())
                && className.startsWith("sm.domain.")
                && className.endsWith("Service")
                && !className.startsWith("sm.domain.sys.monitor.script.");
    }

    List<Method> publicMethods(Class<?> targetClass) {
        return Arrays.stream(targetClass.getMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getDeclaringClass() != Object.class)
                .sorted(Comparator.comparing(Method::getName).thenComparing(Method::toGenericString))
                .toList();
    }

    List<ScriptApiServiceVO> metadata() {
        List<ScriptApiServiceVO> services = new ArrayList<>();
        for (Map.Entry<String, Object> beanEntry : applicationContext.getBeansWithAnnotation(Service.class).entrySet()) {
            String beanName = beanEntry.getKey();
            Object bean = beanEntry.getValue();
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            if (!isAllowedService(targetClass)) {
                continue;
            }
            ScriptApiServiceVO service = new ScriptApiServiceVO();
            service.setBeanName(beanName);
            service.setClassName(targetClass.getName());
            service.setMethods(publicMethods(targetClass).stream()
                    .map(method -> methodMetadata(beanName, method))
                    .toList());
            services.add(service);
        }
        services.sort(Comparator.comparing(ScriptApiServiceVO::getBeanName));
        return services;
    }

    private ScriptApiMethodVO methodMetadata(String beanName, Method method) {
        ScriptApiMethodVO result = new ScriptApiMethodVO();
        result.setName(method.getName());
        result.setReturnType(typeName(method.getGenericReturnType()));
        List<ScriptApiParameterVO> parameters = Arrays.stream(method.getParameters())
                .map(this::parameterMetadata)
                .toList();
        result.setParameters(parameters);
        result.setSignature(method.getName() + "(" + parameters.stream()
                .map(parameter -> parameter.getName() + ": " + parameter.getType())
                .reduce((left, right) -> left + ", " + right).orElse("") + "): " + result.getReturnType());
        result.setExample(example(beanName, method, parameters));
        return result;
    }

    private ScriptApiParameterVO parameterMetadata(Parameter parameter) {
        ScriptApiParameterVO result = new ScriptApiParameterVO();
        result.setName(parameter.getName());
        result.setType(typeName(parameter.getParameterizedType()));
        result.setRequired(isRequired(parameter.getAnnotations()));
        result.setFields(isStructured(parameter.getType()) ? fields(parameter.getType()) : List.of());
        return result;
    }

    private List<ScriptApiFieldVO> fields(Class<?> type) {
        List<Field> sourceFields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            sourceFields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        return sourceFields.stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    ScriptApiFieldVO result = new ScriptApiFieldVO();
                    result.setName(field.getName());
                    result.setType(typeName(field.getGenericType()));
                    result.setRequired(isRequired(field.getAnnotations()));
                    result.setConstraints(constraints(field));
                    return result;
                })
                .sorted(Comparator.comparing(ScriptApiFieldVO::getName))
                .toList();
    }

    private List<String> constraints(Field field) {
        List<String> result = new ArrayList<>();
        Size size = field.getAnnotation(Size.class);
        if (size != null) result.add("长度 " + size.min() + "～" + size.max());
        Min min = field.getAnnotation(Min.class);
        if (min != null) result.add("最小值 " + min.value());
        Max max = field.getAnnotation(Max.class);
        if (max != null) result.add("最大值 " + max.value());
        Pattern pattern = field.getAnnotation(Pattern.class);
        if (pattern != null) result.add("格式 " + pattern.regexp());
        return result;
    }

    private String example(String beanName, Method method, List<ScriptApiParameterVO> parameters) {
        List<Object> arguments = new ArrayList<>();
        for (int index = 0; index < parameters.size(); index++) {
            arguments.add(exampleValue(method.getParameterTypes()[index], parameters.get(index).getFields()));
        }
        try {
            String argumentText = arguments.stream().map(argument -> {
                try {
                    return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(argument);
                } catch (JacksonException exception) {
                    return "null";
                }
            }).reduce((left, right) -> left + ", " + right).orElse("");
            return "const service = app.getService('" + beanName + "');\n"
                    + "const result = service." + method.getName() + "(" + argumentText + ");\n"
                    + "console.log(result);\nreturn result;";
        } catch (RuntimeException exception) {
            return "const service = app.getService('" + beanName + "');\nreturn service."
                    + method.getName() + "();";
        }
    }

    private Object exampleValue(Class<?> type, List<ScriptApiFieldVO> fields) {
        if (type == String.class || type == char.class || type == Character.class) return "示例值";
        if (type == boolean.class || type == Boolean.class) return true;
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) return 1;
        if (type.isEnum()) return type.getEnumConstants().length == 0 ? null : type.getEnumConstants()[0].toString();
        if (Collection.class.isAssignableFrom(type) || type.isArray()) return List.of();
        Map<String, Object> result = new LinkedHashMap<>();
        fields.forEach(field -> result.put(field.getName(), simpleExample(field.getType())));
        return result;
    }

    private Object simpleExample(String type) {
        if (type.equals("String") || type.contains("LocalDate") || type.contains("Date")) return "示例值";
        if (type.equals("boolean") || type.equals("Boolean")) return true;
        if (type.startsWith("List<") || type.endsWith("[]")) return List.of();
        if (type.matches("(byte|short|int|long|float|double|Byte|Short|Integer|Long|Float|Double|BigDecimal)")) return 1;
        return null;
    }

    private boolean isRequired(Annotation[] annotations) {
        return Arrays.stream(annotations).anyMatch(annotation -> REQUIRED_ANNOTATIONS.contains(annotation.annotationType()));
    }

    private boolean isStructured(Class<?> type) {
        return !type.isPrimitive() && !type.isEnum() && !type.isArray()
                && !type.getPackageName().startsWith("java.") && !Temporal.class.isAssignableFrom(type);
    }

    private String typeName(Type type) {
        if (type instanceof Class<?> targetClass) {
            return targetClass.isArray() ? typeName(targetClass.getComponentType()) + "[]" : targetClass.getSimpleName();
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return typeName(parameterizedType.getRawType()) + "<" + Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(this::typeName).reduce((left, right) -> left + ", " + right).orElse("") + ">";
        }
        return type.getTypeName().replace("java.lang.", "");
    }

    record ServiceTarget(Object bean, Class<?> targetClass) {
    }
}
