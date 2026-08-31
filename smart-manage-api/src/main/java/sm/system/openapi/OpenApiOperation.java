package sm.system.openapi;

/** 由 API 提供模块显式注册的稳定操作定义。 */
public record OpenApiOperation(String operationKey, String apiNumber, String apiVersion,
                               String name, String httpMethod, String path,
                               String domainKey, String domainName,
                               String applicationKey, String applicationName,
                               String featureKey, String featureName) {
}
