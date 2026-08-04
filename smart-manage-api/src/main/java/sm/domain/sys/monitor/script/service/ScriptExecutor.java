package sm.domain.sys.monitor.script.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** GraalJS 单节点执行器；Context 按次隔离，Engine 由 GraalJS 内部管理。 */
@Component
@RequiredArgsConstructor
class ScriptExecutor {
    private final ScriptServiceGateway serviceGateway;
    private final JsonMapper jsonMapper;
    private final Semaphore executionPermit = new Semaphore(1);
    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "script-console-timeout");
        thread.setDaemon(true);
        return thread;
    });

    ScriptExecutionOutcome execute(ScriptExecutionConfig config, String content) {
        if (!executionPermit.tryAcquire()) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "已有脚本正在执行，请稍后重试");
        }
        try {
            return executeExclusive(config, content);
        } finally {
            executionPermit.release();
        }
    }

    private ScriptExecutionOutcome executeExclusive(ScriptExecutionConfig config, String content) {
        Instant start = Instant.now();
        BoundedOutputStream output = new BoundedOutputStream(config.maxOutputLength());
        AtomicBoolean timedOut = new AtomicBoolean(false);
        try (PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            Context context = Context.newBuilder("js")
                    // 普通 HotSpot JVM 会使用安全可用的解释器运行；不要把引擎性能提示混入用户脚本输出。
                    .option("engine.WarnInterpreterOnly", "false")
                     .allowHostAccess(HostAccess.NONE)
                     .allowHostClassLookup(className -> false)
                     .allowCreateThread(false)
                     .allowCreateProcess(false)
                     .allowNativeAccess(false)
                     .allowIO(false)
                     .out(printStream)
                    .err(printStream)
                    .build();
            context.getBindings("js").putMember("app", serviceGateway.createBinding());
            ScheduledFuture<?> timeout = timeoutExecutor.schedule(() -> {
                timedOut.set(true);
                context.close(true);
            }, config.timeoutSeconds(), TimeUnit.SECONDS);
            try {
                Value returnValue = context.eval("js", "(function () {\n" + content + "\n})()");
                appendReturnValue(output, returnValue);
                return outcome("SUCCESS", output, null, start);
            } catch (PolyglotException exception) {
                String status = timedOut.get() || exception.isCancelled() ? "TIMEOUT" : "ERROR";
                String message = "TIMEOUT".equals(status)
                        ? "脚本执行超过 " + config.timeoutSeconds() + " 秒，已取消"
                        : safeMessage(exception);
                return outcome(status, output, message, start);
            } catch (RuntimeException exception) {
                return outcome("ERROR", output, safeMessage(exception), start);
            } finally {
                timeout.cancel(false);
                try {
                    context.close();
                } catch (PolyglotException ignored) {
                    // 已被超时任务取消的 Context 再次关闭时可能抛取消异常，不改变本次执行结果。
                }
            }
        }
    }

    private void appendReturnValue(BoundedOutputStream output, Value value) {
        if (value == null || value.isNull()) {
            return;
        }
        String text;
        if (value.isString()) {
            text = value.asString();
        } else if (value.isBoolean()) {
            text = Boolean.toString(value.asBoolean());
        } else if (value.fitsInLong()) {
            text = Long.toString(value.asLong());
        } else if (value.fitsInDouble()) {
            text = Double.toString(value.asDouble());
        } else {
            try {
                text = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plainValue(value));
            } catch (JacksonException exception) {
                text = value.toString();
            }
        }
        if (!text.isBlank()) {
            byte[] prefix = output.content().isBlank() ? new byte[0] : System.lineSeparator().getBytes(StandardCharsets.UTF_8);
            output.write(prefix, 0, prefix.length);
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            output.write(bytes, 0, bytes.length);
        }
    }

    private Object plainValue(Value value) {
        if (value == null || value.isNull()) return null;
        if (value.isBoolean()) return value.asBoolean();
        if (value.isString()) return value.asString();
        if (value.fitsInLong()) return value.asLong();
        if (value.fitsInDouble()) return value.asDouble();
        if (value.hasArrayElements()) {
            java.util.List<Object> values = new java.util.ArrayList<>();
            for (long index = 0; index < value.getArraySize(); index++) {
                values.add(plainValue(value.getArrayElement(index)));
            }
            return values;
        }
        if (value.hasMembers()) {
            java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
            for (String key : value.getMemberKeys()) {
                values.put(key, plainValue(value.getMember(key)));
            }
            return values;
        }
        return value.toString();
    }

    private ScriptExecutionOutcome outcome(String status, BoundedOutputStream output, String error, Instant start) {
        int duration = Math.toIntExact(Duration.between(start, Instant.now()).toMillis());
        return new ScriptExecutionOutcome(status, output.content(), error, duration, output.isTruncated());
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }

    @PreDestroy
    void shutdown() {
        timeoutExecutor.shutdownNow();
    }
}
