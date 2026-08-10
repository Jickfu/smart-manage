package sm.domain.sys.monitor.thread.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadDiagnosticAccessorTests {
    private final ThreadDiagnosticAccessor accessor = new ThreadDiagnosticAccessor();

    ThreadDiagnosticAccessorTests() {
        ReflectionTestUtils.setField(accessor, "instanceId", "test-instance");
    }

    @Test
    void listReturnsCurrentPlatformThreadsWithoutStacks() {
        var result = accessor.list();

        assertEquals("test-instance", result.getInstanceId());
        assertFalse(result.getThreads().isEmpty());
        assertTrue(result.getThreads().stream().allMatch(thread -> thread.getStackTrace().isEmpty()));
    }

    @Test
    void detailReturnsCurrentThreadStack() {
        var result = accessor.detail(Thread.currentThread().threadId(), 32);

        assertEquals(1, result.getThreads().size());
        assertEquals(Thread.currentThread().threadId(), result.getThreads().getFirst().getId());
        assertFalse(result.getThreads().getFirst().getStackTrace().isEmpty());
    }

    @Test
    void hotSamplingReturnsBoundedThreadListWithStacks() {
        var result = accessor.hot(200, 3, 16);

        assertEquals(200, result.getSampleMillis());
        assertTrue(result.getThreads().size() <= 3);
        assertTrue(result.getThreads().stream().allMatch(thread -> thread.getCpuUsage() != null));
    }
}
