package sm.system.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceIdUtilTests {

	@AfterEach
	void clearTraceId() {
		TraceIdUtil.clear();
	}

	@Test
	void wrappedTaskPropagatesTraceIdAndCleansPooledThread() throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// 先创建工作线程，证明传播不是依赖 InheritableThreadLocal 的线程创建时继承。
			assertNull(executor.submit(TraceIdUtil::getTraceId).get());
			TraceIdUtil.setTraceId("request-trace");

			Future<String> propagated = executor.submit(
					TraceIdUtil.wrap((java.util.concurrent.Callable<String>) TraceIdUtil::getTraceId));

			assertEquals("request-trace", propagated.get());
			assertNull(executor.submit(TraceIdUtil::getTraceId).get());
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void invalidIncomingTraceIdIsReplaced() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader(TraceIdUtil.TRACE_ID_STRING)).thenReturn("invalid trace\r\nheader");

		String traceId = TraceIdUtil.generateTraceId(request);

		assertNotEquals("invalid trace\r\nheader", traceId);
		assertEquals(36, traceId.length());
	}
}
