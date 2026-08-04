package sm.domain.sys.monitor.script.service;

import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 只把公开领域 Service 映射成 Polyglot Proxy，不向脚本泄露 Bean、Class 或反射对象。
 * Java 返回值会先转换为 JSON 兼容数据，防止脚本沿返回对象继续访问宿主能力。
 */
@Component
@RequiredArgsConstructor
class ScriptServiceGateway {
    private final ScriptServiceCatalog serviceCatalog;
    private final JsonMapper jsonMapper;

    ProxyObject createBinding() {
        Map<String, Object> members = new HashMap<>();
        members.put("getService", (ProxyExecutable) arguments -> {
            if (arguments.length != 1 || !arguments[0].isString()) {
                throw new IllegalArgumentException("app.getService(name) 需要一个服务 Bean 名称");
            }
            return serviceProxy(arguments[0].asString());
        });
        return ProxyObject.fromMap(members);
    }

    private ProxyObject serviceProxy(String beanName) {
        ScriptServiceCatalog.ServiceTarget target = serviceCatalog.requireService(beanName);
        Object bean = target.bean();
        Class<?> targetClass = target.targetClass();
        Map<String, Object> methods = new TreeMap<>();
        serviceCatalog.publicMethods(targetClass).stream()
                .forEach(method -> methods.putIfAbsent(method.getName(),
                        (ProxyExecutable) arguments -> invoke(bean, targetClass, method.getName(), arguments)));
        return ProxyObject.fromMap(methods);
    }

    private Object invoke(Object bean, Class<?> targetClass, String methodName, Value[] arguments) {
        List<Method> candidates = Arrays.stream(targetClass.getMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterCount() == arguments.length)
                .toList();
        RuntimeException conversionFailure = null;
        for (Method method : candidates) {
            try {
                Object[] converted = convertArguments(method, arguments);
                Object result = method.invoke(bean, converted);
                return toGuestValue(result == null ? null : jsonMapper.convertValue(result, Object.class));
            } catch (IllegalArgumentException exception) {
                conversionFailure = exception;
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("服务方法不可访问：" + methodName, exception);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException(cause.getMessage(), cause);
            }
        }
        throw new IllegalArgumentException("没有匹配的服务方法：" + targetClass.getSimpleName() + "." + methodName,
                conversionFailure);
    }

    private Object[] convertArguments(Method method, Value[] arguments) {
        Object[] converted = new Object[arguments.length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int index = 0; index < arguments.length; index++) {
            Value argument = arguments[index];
            Object rawValue = guestValue(argument);
            converted[index] = jsonMapper.convertValue(rawValue, parameterTypes[index]);
        }
        return converted;
    }

    private Object guestValue(Value value) {
        if (value == null || value.isNull()) return null;
        if (value.isBoolean()) return value.asBoolean();
        if (value.isString()) return value.asString();
        if (value.fitsInLong()) return value.asLong();
        if (value.fitsInDouble()) return value.asDouble();
        if (value.hasArrayElements()) {
            List<Object> values = new ArrayList<>();
            for (long index = 0; index < value.getArraySize(); index++) {
                values.add(guestValue(value.getArrayElement(index)));
            }
            return values;
        }
        if (value.hasMembers()) {
            Map<String, Object> values = new LinkedHashMap<>();
            for (String key : value.getMemberKeys()) {
                values.put(key, guestValue(value.getMember(key)));
            }
            return values;
        }
        return value.toString();
    }

    /** 只向脚本返回 Polyglot 基础值和 Proxy，避免泄露 Java 返回对象的宿主能力。 */
    private Object toGuestValue(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> members = new LinkedHashMap<>();
            source.forEach((key, item) -> members.put(String.valueOf(key), toGuestValue(item)));
            return ProxyObject.fromMap(members);
        }
        if (value instanceof Collection<?> source) {
            return ProxyArray.fromList(source.stream().map(this::toGuestValue).toList());
        }
        throw new IllegalArgumentException("服务返回值无法转换为脚本安全类型：" + value.getClass().getName());
    }
}
