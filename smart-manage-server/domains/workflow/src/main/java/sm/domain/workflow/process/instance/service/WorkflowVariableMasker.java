package sm.domain.workflow.process.instance.service;

import org.springframework.stereotype.Component;
import sm.system.aop.log.LogPayloadUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 管理端流程变量投影；保留排障所需结构，同时不返回凭据类变量值。 */
@Component
public class WorkflowVariableMasker {
    private static final int MAX_DEPTH = 20;

    public Map<String, Object> mask(Map<String, Object> variables) {
        Map<String, Object> result = new LinkedHashMap<>();
        variables.forEach((key, value) -> result.put(key, sensitive(key) ? "***" : maskValue(value, 1)));
        return java.util.Collections.unmodifiableMap(result);
    }

    private Object maskValue(Object value, int depth) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (depth > MAX_DEPTH) return "[内容层级过深]";
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, child) -> {
                String name = String.valueOf(key);
                result.put(name, sensitive(name) ? "***" : maskValue(child, depth + 1));
            });
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(child -> result.add(maskValue(child, depth + 1)));
            return result;
        }
        if (value.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            for (int index = 0; index < Array.getLength(value); index++) {
                result.add(maskValue(Array.get(value, index), depth + 1));
            }
            return result;
        }
        return String.valueOf(value);
    }

    private static boolean sensitive(String key) {
        return "***".equals(LogPayloadUtil.maskNameLike(key));
    }
}
