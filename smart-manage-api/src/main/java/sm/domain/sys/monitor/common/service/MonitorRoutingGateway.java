package sm.domain.sys.monitor.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.monitor.common.config.MonitorClusterProperties;
import sm.system.exception.BizException;
import sm.system.http.HttpClientHelper;
import sm.system.http.HttpRequestSpec;
import sm.system.http.HttpResponseData;
import sm.system.response.ResultEnum;
import sm.system.security.CsrfTokenManager;
import sm.system.util.ServletUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.time.Duration;

/** 将已鉴权请求定向到注册表中的目标实例，目标节点仍会再次执行登录和权限校验。 */
@Component
@RequiredArgsConstructor
public class MonitorRoutingGateway {
    private final JsonMapper jsonMapper;
    private final MonitorClusterProperties properties;
    private final CurrentUserContext currentUserContext;
    private final HttpClientHelper httpClientHelper;

    @Value("${sa-token.token-name:smtoken}")
    private String tokenName;

    public <T> T get(MonitorInstanceRegistry.RegisteredInstance instance, String path, Class<T> responseType) {
        HttpRequestSpec request = requestBuilder(instance, path, "GET").build();
        return execute(request, responseType);
    }

    public <T> T post(MonitorInstanceRegistry.RegisteredInstance instance, String path,
                      Object body, Class<T> responseType) {
        try {
            HttpRequestSpec request = requestBuilder(instance, path, "POST").body(body).build();
            return execute(request, responseType);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "构造实例诊断请求失败");
        }
    }

    private HttpRequestSpec.Builder requestBuilder(MonitorInstanceRegistry.RegisteredInstance instance,
                                                   String path, String method) {
        HttpRequestSpec.Builder builder = HttpRequestSpec.builder(
                        method, URI.create(instance.getInternalBaseUrl() + path))
                .timeout(Duration.ofMillis(properties.getRequestTimeoutMs()))
                .header("Cookie", tokenName + "=" + currentUserContext.getToken());
        if ("POST".equals(method)) {
            // 跨节点诊断延续原浏览器请求的安全上下文，目标节点仍执行完整认证与 CSRF 校验。
            builder.header("Origin", ServletUtil.getRequest().getHeader("Origin"));
            builder.header(CsrfTokenManager.HEADER_NAME,
                    ServletUtil.getRequest().getHeader(CsrfTokenManager.HEADER_NAME));
        }
        return builder;
    }

    private <T> T execute(HttpRequestSpec request, Class<T> responseType) {
        try {
            HttpResponseData<String> response = httpClientHelper.execute(request, String.class);
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
        } catch (Exception exception) {
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "目标实例不可访问");
        }
    }
}
