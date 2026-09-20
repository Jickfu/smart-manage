package sm.domain.sys.monitor.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.infrastructure.http.HttpClientProperties;
import sm.system.http.HttpClientHelper;
import sm.system.security.CsrfTokenManager;
import sm.system.security.context.CurrentUserContext;
import tools.jackson.databind.json.JsonMapper;

class MonitorRoutingGatewayTests {
  private HttpServer server;

  @AfterEach
  void stopServer() {
    RequestContextHolder.resetRequestAttributes();
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void forwardsCurrentTokenAsCookieAndReadsStructuredResult() throws Exception {
    AtomicReference<String> forwardedToken = new AtomicReference<>();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/internal",
        exchange -> {
          forwardedToken.set(exchange.getRequestHeaders().getFirst("Cookie"));
          byte[] body =
              "{\"code\":0,\"msg\":\"\",\"data\":{\"instanceId\":\"target\",\"current\":false}}"
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();

    MonitorRoutingGateway gateway = createGateway();
    MonitorInstanceRegistry.RegisteredInstance instance =
        new MonitorInstanceRegistry.RegisteredInstance();
    instance.setInternalBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());

    MonitorInstanceVO result = gateway.get(instance, "/internal", MonitorInstanceVO.class);

    assertEquals("target", result.getInstanceId());
    assertEquals("smtoken=test-token", forwardedToken.get());
  }

  @Test
  void forwardsOriginAndCsrfTokenForPostRequest() throws Exception {
    AtomicReference<String> forwardedOrigin = new AtomicReference<>();
    AtomicReference<String> forwardedCsrfToken = new AtomicReference<>();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/internal",
        exchange -> {
          forwardedOrigin.set(exchange.getRequestHeaders().getFirst("Origin"));
          forwardedCsrfToken.set(
              exchange.getRequestHeaders().getFirst(CsrfTokenManager.HEADER_NAME));
          byte[] body =
              "{\"code\":0,\"msg\":\"\",\"data\":{\"instanceId\":\"target\",\"current\":false}}"
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();
    MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/source");
    servletRequest.addHeader("Origin", "http://localhost:8000");
    servletRequest.addHeader(CsrfTokenManager.HEADER_NAME, "0123456789abcdef0123456789abcdef");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
    MonitorInstanceRegistry.RegisteredInstance instance =
        new MonitorInstanceRegistry.RegisteredInstance();
    instance.setInternalBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());

    createGateway().post(instance, "/internal", java.util.Map.of(), MonitorInstanceVO.class);

    assertEquals("http://localhost:8000", forwardedOrigin.get());
    assertEquals("0123456789abcdef0123456789abcdef", forwardedCsrfToken.get());
  }

  private MonitorRoutingGateway createGateway() {
    CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    when(currentUserContext.getToken()).thenReturn("test-token");
    MonitorProperties properties = new MonitorProperties();
    HttpClientProperties httpClientProperties = new HttpClientProperties();
    JsonMapper jsonMapper = JsonMapper.builder().build();
    HttpClientHelper httpClientHelper =
        new HttpClientHelper(HttpClient.newHttpClient(), jsonMapper, httpClientProperties);
    MonitorRoutingGateway gateway =
        new MonitorRoutingGateway(jsonMapper, properties, currentUserContext, httpClientHelper);
    ReflectionTestUtils.setField(gateway, "tokenName", "smtoken");
    return gateway;
  }
}
