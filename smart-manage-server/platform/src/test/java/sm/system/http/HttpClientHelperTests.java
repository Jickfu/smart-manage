package sm.system.http;

import sm.infrastructure.http.HttpClientProperties;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import sm.system.util.TraceIdUtil;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpClientHelperTests {
    private HttpServer server;
    private HttpClientHelper helper;

    @Test
    @org.junit.jupiter.api.Timeout(10)
    void boundsDeclaredAndChunkedResponsesBeforeParsing() throws Exception {
        var properties = new HttpClientProperties();
        properties.setMaxResponseBytes(8);
        helper = new HttpClientHelper(HttpClient.newHttpClient(), JsonMapper.builder().build(), properties);
        server.createContext("/exact", exchange -> respond(exchange, 200, "12345678"));
        server.createContext("/declared", exchange -> respond(exchange, 200, "secret-too-long"));
        server.createContext("/chunked", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try (var output = exchange.getResponseBody()) {
                output.write("123456789".getBytes(StandardCharsets.UTF_8));
            }
        });
        server.start();
        assertEquals("12345678", helper.execute(HttpRequestSpec.get(uri("/exact")).build(), String.class).body());
        for (String endpoint : List.of("/declared", "/chunked")) {
            var failure = assertThrows(ExternalHttpException.class,
                    () -> helper.execute(HttpRequestSpec.get(uri(endpoint)).build(), String.class));
            assertEquals(ExternalHttpException.FailureType.RESPONSE_TOO_LARGE, failure.getFailureType());
            assertFalse(failure.getMessage().contains("secret"));
        }
        assertThrows(IllegalArgumentException.class, () -> properties.setMaxResponseBytes(0));
    }

    @Test
    @org.junit.jupiter.api.Timeout(10)
    void bodylessResponsesIgnoreResourceLengthAndPreserveHttpStatusMeaning() {
        var properties = new HttpClientProperties();
        properties.setMaxResponseBytes(8);
        helper = new HttpClientHelper(HttpClient.newHttpClient(), JsonMapper.builder().build(), properties);
        server.createContext("/head", exchange -> {
            exchange.getResponseHeaders().set("Content-Length", "9");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/not-modified", exchange -> {
            exchange.getResponseHeaders().set("Content-Length", "9");
            exchange.sendResponseHeaders(304, -1);
            exchange.close();
        });
        for (int status : List.of(204, 205)) {
            server.createContext("/empty-" + status, exchange -> {
                exchange.sendResponseHeaders(status, -1);
                exchange.close();
            });
        }
        server.start();
        var head = helper.execute(HttpRequestSpec.builder("HEAD", uri("/head")).build(), byte[].class);
        assertEquals(200, head.statusCode());
        assertEquals(0, head.body().length);
        var cached = helper.execute(HttpRequestSpec.get(uri("/not-modified"))
                .failOnNonSuccess(false).build(), byte[].class);
        assertEquals(304, cached.statusCode());
        assertEquals(0, cached.body().length);
        var statusFailure = assertThrows(ExternalHttpException.class,
                () -> helper.execute(HttpRequestSpec.get(uri("/not-modified")).build(), byte[].class));
        assertEquals(ExternalHttpException.FailureType.HTTP_STATUS, statusFailure.getFailureType());
        assertEquals(304, statusFailure.getStatusCode());
        for (int status : List.of(204, 205)) {
            var empty = helper.execute(HttpRequestSpec.get(uri("/empty-" + status)).build(), byte[].class);
            assertEquals(status, empty.statusCode());
            assertEquals(0, empty.body().length);
        }
    }

    @Test
    void skippingDeclaredLengthStillRejectsUnexpectedActualBytes() {
        var properties = new HttpClientProperties();
        properties.setMaxResponseBytes(8);
        helper = new HttpClientHelper(HttpClient.newHttpClient(), JsonMapper.builder().build(), properties);
        record BodylessCase(String method, int status) { }
        var cases = List.of(new BodylessCase("HEAD", 200), new BodylessCase("GET", 100),
                new BodylessCase("GET", 101), new BodylessCase("GET", 199),
                new BodylessCase("GET", 204), new BodylessCase("GET", 205), new BodylessCase("GET", 304));
        for (var scenario : cases) {
            var info = org.mockito.Mockito.mock(java.net.http.HttpResponse.ResponseInfo.class);
            org.mockito.Mockito.when(info.statusCode()).thenReturn(scenario.status());
            org.mockito.Mockito.when(info.headers()).thenReturn(java.net.http.HttpHeaders.of(
                    java.util.Map.of("Content-Length", List.of("9")), (name, value) -> true));
            var request = java.net.http.HttpRequest.newBuilder(uri("/unused"))
                    .method(scenario.method(), java.net.http.HttpRequest.BodyPublishers.noBody()).build();
            var subscriber = helper.createResponseBodySubscriber(request, info);
            var upstream = org.mockito.Mockito.mock(java.util.concurrent.Flow.Subscription.class);
            subscriber.onSubscribe(upstream);
            var body = subscriber.getBody().toCompletableFuture();
            assertFalse(body.isDone(), "无正文响应不能因资源声明长度而被拒绝: " + scenario);
            subscriber.onNext(List.of(java.nio.ByteBuffer.wrap(new byte[9])));
            var failure = assertThrows(java.util.concurrent.CompletionException.class, body::join);
            assertEquals(ExternalHttpException.FailureType.RESPONSE_TOO_LARGE,
                    ((ExternalHttpException) failure.getCause()).getFailureType());
            org.mockito.Mockito.verify(upstream).cancel();
        }
    }

    @Test
    void rejectionCompletesBodyBeforeCancellingUpstream() {
        var subscriber = new BoundedHttpBodySubscriber(8, 9);
        var upstream = org.mockito.Mockito.mock(java.util.concurrent.Flow.Subscription.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            assertTrue(subscriber.getBody().toCompletableFuture().isCompletedExceptionally());
            return null;
        }).when(upstream).cancel();
        subscriber.onSubscribe(upstream);
        org.mockito.Mockito.verify(upstream).cancel();
    }

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        HttpClientProperties properties = new HttpClientProperties();
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setRequestTimeout(Duration.ofSeconds(2));
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        helper = new HttpClientHelper(client, JsonMapper.builder().build(), properties);
    }

    @AfterEach
    void tearDown() {
        TraceIdUtil.clear();
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsEncodedQueryCustomHeadersCookieAndTraceId() {
        AtomicReference<String> rawQuery = new AtomicReference<>();
        AtomicReference<String> cookie = new AtomicReference<>();
        AtomicReference<String> customHeader = new AtomicReference<>();
        AtomicReference<String> traceId = new AtomicReference<>();
        server.createContext("/weather", exchange -> {
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            cookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
            customHeader.set(exchange.getRequestHeaders().getFirst("X-Api-Key"));
            traceId.set(exchange.getRequestHeaders().getFirst("traceId"));
            respond(exchange, 200, "{\"city\":\"广州\"}");
        });
        server.start();
        TraceIdUtil.setTraceId("trace-http-test");

        HttpRequestSpec request = HttpRequestSpec.get(uri("/weather"))
                .queryParam("city", "广州")
                .queryParam("keyword", "a b")
                .header("Cookie", "session=test-cookie")
                .header("X-Api-Key", "test-key")
                .build();

        HttpResponseData<WeatherData> response = helper.execute(request, WeatherData.class);

        assertEquals(200, response.statusCode());
        assertEquals("广州", response.body().city());
        assertTrue(rawQuery.get().contains("city=%E5%B9%BF%E5%B7%9E"));
        assertTrue(rawQuery.get().contains("keyword=a%20b"));
        assertEquals("session=test-cookie", cookie.get());
        assertEquals("test-key", customHeader.get());
        assertEquals("trace-http-test", traceId.get());
    }

    @Test
    void serializesJsonBodyAndReadsParameterizedResponse() {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> contentType = new AtomicReference<>();
        server.createContext("/forecast", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            respond(exchange, 200, "{\"items\":[{\"city\":\"深圳\"}]}");
        });
        server.start();
        HttpRequestSpec request = HttpRequestSpec.post(uri("/forecast"))
                .body(new WeatherData("广州"))
                .build();

        HttpResponseData<ItemPage<WeatherData>> response = helper.execute(
                request, new ParameterizedTypeReference<>() {
                });

        assertEquals("application/json", contentType.get());
        assertTrue(requestBody.get().contains("\"city\":\"广州\""));
        assertEquals("深圳", response.body().items().getFirst().city());
    }

    @Test
    void classifiesNonSuccessWithoutExposingResponseBody() {
        server.createContext("/unauthorized", exchange ->
                respond(exchange, 401, "credential=should-not-appear"));
        server.start();

        ExternalHttpException exception = assertThrows(ExternalHttpException.class,
                () -> helper.execute(HttpRequestSpec.get(uri("/unauthorized")).build(), String.class));

        assertEquals(ExternalHttpException.FailureType.HTTP_STATUS, exception.getFailureType());
        assertEquals(401, exception.getStatusCode());
        assertFalse(exception.getMessage().contains("should-not-appear"));
    }

    @Test
    void permitsCallerToHandleNonSuccessExplicitly() {
        server.createContext("/not-found", exchange -> respond(exchange, 404, "missing"));
        server.start();
        HttpRequestSpec request = HttpRequestSpec.get(uri("/not-found"))
                .failOnNonSuccess(false)
                .build();

        HttpResponseData<String> response = helper.execute(request, String.class);

        assertEquals(404, response.statusCode());
        assertEquals("missing", response.body());
        assertFalse(response.isSuccess());
    }

    @Test
    void classifiesTimeoutAndInvalidJson() {
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(300);
                respond(exchange, 200, "{}");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                exchange.close();
            } catch (IOException ignored) {
                // 客户端超时关闭连接是本测试的预期结果。
            }
        });
        server.createContext("/invalid-json", exchange -> respond(exchange, 200, "not-json"));
        server.start();

        HttpRequestSpec slowRequest = HttpRequestSpec.get(uri("/slow"))
                .timeout(Duration.ofMillis(50))
                .build();
        ExternalHttpException timeoutException = assertThrows(ExternalHttpException.class,
                () -> helper.execute(slowRequest, WeatherData.class));
        ExternalHttpException parseException = assertThrows(ExternalHttpException.class,
                () -> helper.execute(HttpRequestSpec.get(uri("/invalid-json")).build(), WeatherData.class));

        assertEquals(ExternalHttpException.FailureType.TIMEOUT, timeoutException.getFailureType());
        assertEquals(ExternalHttpException.FailureType.RESPONSE_PARSE, parseException.getFailureType());
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    private void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private record WeatherData(String city) {
    }

    private record ItemPage<T>(List<T> items) {
    }
}
