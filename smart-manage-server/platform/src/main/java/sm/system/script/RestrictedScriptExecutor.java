package sm.system.script;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 无宿主能力的通用 JavaScript 沙箱。调用方只传 JSON 数据，脚本不能访问 Spring Bean、Java 类、文件、网络或进程。
 */
@Component
@RequiredArgsConstructor
public class RestrictedScriptExecutor {
    private static final int MAX_RESULT_DEPTH = 20;
    private static final int MAX_RESULT_NODES = 2_000;
    private static final int MAX_RESULT_COLLECTION_SIZE = 1_000;
    private static final int MAX_RESULT_BYTES = 100 * 1024;
    private final JsonMapper json;
    private final java.util.concurrent.ScheduledExecutorService timeoutExecutor =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "restricted-script-timeout");
                thread.setDaemon(true);
                return thread;
            });
    private final java.util.concurrent.Semaphore concurrency = new java.util.concurrent.Semaphore(4);

    public Outcome execute(String source, Map<String, Object> context, int timeoutSeconds) {
        if (source == null || source.isBlank() || source.length() > 20_000) {
            return new Outcome("ERROR", null, "脚本内容为空或超过长度限制", 0);
        }
        int boundedTimeout = Math.max(1, Math.min(timeoutSeconds, 10));
        if (!concurrency.tryAcquire()) return new Outcome("ERROR", null, "脚本执行资源繁忙", 0);
        try {
            return executeAcquired(source, context, boundedTimeout);
        } finally {
            concurrency.release();
        }
    }

    private Outcome executeAcquired(String source, Map<String, Object> context, int boundedTimeout) {
        Instant start = Instant.now();
        AtomicBoolean timedOut = new AtomicBoolean(false);
        try (Context polyglot = Context.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .allowHostAccess(HostAccess.NONE)
                .allowHostClassLookup(className -> false)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .allowNativeAccess(false)
                .allowIO(false)
                .build()) {
            String contextJson = json.writeValueAsString(context);
            String contextLiteral = json.writeValueAsString(contextJson);
            var timeout = timeoutExecutor.schedule(() -> {
                timedOut.set(true);
                polyglot.close(true);
            }, boundedTimeout, TimeUnit.SECONDS);
            try {
                Value value = polyglot.eval("js", "const workflow = JSON.parse(" + contextLiteral
                        + ");\n(function () {\n" + source + "\n})()");
                return new Outcome("SUCCESS", plain(value, new ResultBudget(), 0,
                        new IdentityHashMap<>()), null, duration(start));
            } catch (PolyglotException failure) {
                String message = timedOut.get() || failure.isCancelled()
                        ? "脚本执行超时" : safeMessage(failure);
                return new Outcome(timedOut.get() || failure.isCancelled() ? "TIMEOUT" : "ERROR",
                        null, message, duration(start));
            } catch (RuntimeException failure) {
                return new Outcome("ERROR", null, safeMessage(failure), duration(start));
            } finally {
                timeout.cancel(false);
            }
        }
    }

    private Object plain(Value value, ResultBudget budget, int depth,
                         IdentityHashMap<Value, Boolean> visiting) {
        budget.node(depth);
        if (value == null || value.isNull()) return null;
        if (value.isBoolean()) return value.asBoolean();
        if (value.isString()) {
            String text = value.asString();
            budget.text(text);
            return text;
        }
        if (value.fitsInLong()) return value.asLong();
        if (value.fitsInDouble()) return value.asDouble();
        if (visiting.put(value, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("脚本返回值不能包含循环引用");
        }
        try {
            return composite(value, budget, depth, visiting);
        } finally {
            visiting.remove(value);
        }
    }

    private Object composite(Value value, ResultBudget budget, int depth,
                             IdentityHashMap<Value, Boolean> visiting) {
        if (value.hasArrayElements()) {
            long size = value.getArraySize();
            if (size > MAX_RESULT_COLLECTION_SIZE) {
                throw new IllegalArgumentException("脚本返回数组超过元素数量限制");
            }
            var values = new java.util.ArrayList<>((int) size);
            for (long index = 0; index < size; index++) {
                values.add(plain(value.getArrayElement(index), budget, depth + 1, visiting));
            }
            return values;
        }
        if (value.hasMembers()) {
            var memberKeys = value.getMemberKeys();
            if (memberKeys.size() > MAX_RESULT_COLLECTION_SIZE) {
                throw new IllegalArgumentException("脚本返回对象超过成员数量限制");
            }
            Map<String, Object> values = new LinkedHashMap<>();
            for (String key : memberKeys) {
                budget.text(key);
                values.put(key, plain(value.getMember(key), budget, depth + 1, visiting));
            }
            return values;
        }
        throw new IllegalArgumentException("脚本返回值必须是 JSON 可表达的数据");
    }

    private static final class ResultBudget {
        private int nodes;
        private int bytes;

        private void node(int depth) {
            if (depth > MAX_RESULT_DEPTH) throw new IllegalArgumentException("脚本返回值层级过深");
            if (++nodes > MAX_RESULT_NODES) throw new IllegalArgumentException("脚本返回值节点数量超过限制");
        }

        private void text(String value) {
            bytes = Math.addExact(bytes, value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
            if (bytes > MAX_RESULT_BYTES) throw new IllegalArgumentException("脚本返回值超过100KB限制");
        }
    }

    private static int duration(Instant start) {
        return Math.toIntExact(Duration.between(start, Instant.now()).toMillis());
    }

    private static String safeMessage(Throwable failure) {
        String message = failure.getMessage();
        if (message == null || message.isBlank()) return failure.getClass().getSimpleName();
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    @PreDestroy
    void shutdown() {
        timeoutExecutor.shutdownNow();
    }

    public record Outcome(String status, Object value, String error, int durationMs) { }
}
