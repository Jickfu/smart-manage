package sm.system.openapi;

/** 由 API 提供模块显式注册的稳定操作定义。 */
public record OpenApiOperation(String operationKey, String apiNumber, String apiVersion,
                               String name, String httpMethod, String path,
                               String domainKey, String domainName,
                               String applicationKey, String applicationName,
                               String featureKey, String featureName,
                               OpenApiTestHandler testHandler) {

    public OpenApiOperation(String operationKey, String apiNumber, String apiVersion,
                            String name, String httpMethod, String path,
                            String domainKey, String domainName,
                            String applicationKey, String applicationName,
                            String featureKey, String featureName) {
        this(operationKey, apiNumber, apiVersion, name, httpMethod, path, domainKey, domainName,
                applicationKey, applicationName, featureKey, featureName, null);
    }

    public boolean testable() {
        return testHandler != null;
    }
}
