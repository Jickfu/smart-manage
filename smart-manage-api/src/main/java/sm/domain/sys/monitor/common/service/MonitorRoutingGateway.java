package sm.domain.sys.monitor.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.common.config.MonitorClusterProperties;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** 将已鉴权请求定向到注册表中的目标实例，目标节点仍会再次执行登录和权限校验。 */
@Component
@RequiredArgsConstructor
public class MonitorRoutingGateway {
    private final JsonMapper jsonMapper;
    private final MonitorClusterProperties properties;
    private final CurrentUserContext currentUserContext;

    @Value("${sa-token.token-name:smtoken}")
    private String tokenName;

    public <T> T get(MonitorInstanceRegistry.RegisteredInstance instance, String path, Class<T> responseType) {
        HttpRequest request = requestBuilder(instance, path).GET().build();
        return execute(request, responseType);
    }

    public <T> T post(MonitorInstanceRegistry.RegisteredInstance instance, String path,
                      Object body, Class<T> responseType) {
        try {
            HttpRequest request = requestBuilder(instance, path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body)))
                    .build();
            return execute(request, responseType);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "构造实例诊断请求失败");
        }
    }

    private HttpRequest.Builder requestBuilder(MonitorInstanceRegistry.RegisteredInstance instance, String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(instance.getInternalBaseUrl() + path))
                .timeout(Duration.ofMillis(properties.getRequestTimeoutMs()))
                .header(tokenName, currentUserContext.getToken());
    }

    private <T> T execute(HttpRequest request, Class<T> responseType) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "目标实例返回异常状态");
            }
            JsonNode root = jsonMapper.readTree(response.body());
            if (root.path("code").asInt(-1) != 0) {
                throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR,
                        root.path("msg").asText("目标实例诊断失败"));
            }
            return jsonMapper.treeToValue(root.get("data"), responseType);
        } catch (BizException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "实例诊断请求被中断");
        } catch (Exception exception) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "目标实例不可访问");
        }
    }
}
