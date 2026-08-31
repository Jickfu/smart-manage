package sm.system.openapi;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 进程内只读 API 操作注册表；发布元数据由迁移显式持久化。 */
@Component
public class OpenApiOperationRegistry {
    private final Map<String, OpenApiOperation> operationByRoute;
    private final Map<String, OpenApiOperation> operationByKey;

    public OpenApiOperationRegistry(List<OpenApiOperationContributor> contributors) {
        Map<String, OpenApiOperation> routes = new LinkedHashMap<>();
        Map<String, OpenApiOperation> keys = new LinkedHashMap<>();
        for (OpenApiOperationContributor contributor : contributors) {
            Collection<OpenApiOperation> operations = contributor.operations();
            for (OpenApiOperation operation : operations) {
                String routeKey = routeKey(operation.httpMethod(), operation.path());
                if (routes.putIfAbsent(routeKey, operation) != null) {
                    throw new IllegalStateException("OpenAPI 路由重复注册: " + routeKey);
                }
                if (keys.putIfAbsent(operation.operationKey(), operation) != null) {
                    throw new IllegalStateException("OpenAPI operationKey 重复注册: " + operation.operationKey());
                }
            }
        }
        operationByRoute = Map.copyOf(routes);
        operationByKey = Map.copyOf(keys);
    }

    public OpenApiOperation find(String method, String path) {
        return operationByRoute.get(routeKey(method, path));
    }

    public Collection<OpenApiOperation> all() {
        return operationByKey.values();
    }

    private static String routeKey(String method, String path) {
        return method.toUpperCase() + " " + path;
    }
}
