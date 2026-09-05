package sm.system.openapi;

import tools.jackson.databind.JsonNode;

/** API 提供模块显式声明的管理端业务试调处理器；缺少处理器的操作默认禁止试调。 */
@FunctionalInterface
public interface OpenApiTestHandler {
    Object execute(JsonNode request);
}
