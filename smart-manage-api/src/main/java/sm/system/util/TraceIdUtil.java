package sm.system.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * TraceId工具类
 *
 * @author Chekfu
 */
public class TraceIdUtil {

	public static final String TRACE_ID_STRING = "traceId";

	private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

	public static String generateTraceId(HttpServletRequest request) {
		String header = request.getHeader(TRACE_ID_STRING);
		if (header != null) {
			String candidate = header.trim();
			if (!candidate.isEmpty() && candidate.length() <= 64
					&& candidate.matches("[A-Za-z0-9._-]+")) {
				return candidate;
			}
		}
		return UUID.randomUUID().toString();
	}

	public static String getTraceId() {
		return TRACE_ID.get();
	}

	public static void setTraceId(String traceId) {
		if (traceId == null || traceId.isBlank()) {
			clear();
			return;
		}
		TRACE_ID.set(traceId);
	}

	public static void setTraceId(HttpServletRequest request) {
		String traceId = getTraceId();
		if (traceId == null) {
			traceId = generateTraceId(request);
			TRACE_ID.set(traceId);
		}
	}

	public static void clear() {
		TRACE_ID.remove();
	}

	/**
	 * 捕获提交线程的 Trace ID，并确保执行线程结束后清理，避免线程池复用串链路。
	 */
	public static Runnable wrap(Runnable action) {
		String capturedTraceId = getTraceId();
		return () -> {
			String previousTraceId = getTraceId();
			try {
				setTraceId(capturedTraceId);
				action.run();
			} finally {
				setTraceId(previousTraceId);
			}
		};
	}

	/**
	 * Callable 版本的 Trace ID 上下文传播。
	 */
	public static <T> Callable<T> wrap(Callable<T> action) {
		String capturedTraceId = getTraceId();
		return () -> {
			String previousTraceId = getTraceId();
			try {
				setTraceId(capturedTraceId);
				return action.call();
			} finally {
				setTraceId(previousTraceId);
			}
		};
	}
}
