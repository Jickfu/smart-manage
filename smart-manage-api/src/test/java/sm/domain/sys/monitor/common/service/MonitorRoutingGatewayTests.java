package sm.domain.sys.monitor.common.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.common.config.MonitorClusterProperties;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.system.http.HttpClientHelper;
import sm.system.http.HttpClientProperties;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitorRoutingGatewayTests {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void forwardsCurrentTokenAndReadsStructuredResult() throws Exception {
        AtomicReference<String> forwardedToken = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal", exchange -> {
            forwardedToken.set(exchange.getRequestHeaders().getFirst("smtoken"));
            byte[] body = "{\"code\":0,\"msg\":\"\",\"data\":{\"instanceId\":\"target\",\"current\":false}}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        when(currentUserContext.getToken()).thenReturn("test-token");
        MonitorClusterProperties properties = new MonitorClusterProperties();
        HttpClientProperties httpClientProperties = new HttpClientProperties();
        JsonMapper jsonMapper = JsonMapper.builder().build();
        HttpClientHelper httpClientHelper = new HttpClientHelper(
                HttpClient.newHttpClient(), jsonMapper, httpClientProperties);
        MonitorRoutingGateway gateway = new MonitorRoutingGateway(
                jsonMapper, properties, currentUserContext, httpClientHelper);
        ReflectionTestUtils.setField(gateway, "tokenName", "smtoken");
        MonitorInstanceRegistry.RegisteredInstance instance = new MonitorInstanceRegistry.RegisteredInstance();
        instance.setInternalBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());

        MonitorInstanceVO result = gateway.get(instance, "/internal", MonitorInstanceVO.class);

        assertEquals("target", result.getInstanceId());
        assertEquals("test-token", forwardedToken.get());
    }
}
