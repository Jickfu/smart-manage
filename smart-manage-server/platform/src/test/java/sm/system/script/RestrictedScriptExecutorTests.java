package sm.system.script;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestrictedScriptExecutorTests {
    private final RestrictedScriptExecutor executor = new RestrictedScriptExecutor(JsonMapper.builder().build());

    @AfterEach
    void close() {
        executor.shutdown();
    }

    @Test
    void returnsPlainJsonDataFromReadOnlyWorkflowContext() {
        var outcome = executor.execute(
                "return { variables: { total: workflow.variables.amount + 1 }, participants: [20] };",
                Map.of("variables", Map.of("amount", 2)), 1);
        assertEquals("SUCCESS", outcome.status());
        assertInstanceOf(Map.class, outcome.value());
        assertEquals(3L, ((Map<?, ?>) ((Map<?, ?>) outcome.value()).get("variables")).get("total"));
    }

    @Test
    void deniesJavaHostAccess() {
        var outcome = executor.execute("return Java.type('java.lang.System').getenv();", Map.of(), 1);
        assertEquals("ERROR", outcome.status());
        assertNull(outcome.value());
    }

    @Test
    void rejectsCyclicReturnValueWithoutRecursingUntilJvmFailure() {
        var outcome = executor.execute("const value = {}; value.self = value; return value;", Map.of(), 1);

        assertEquals("ERROR", outcome.status());
        assertTrue(outcome.error().contains("循环引用") || outcome.error().contains("层级过深"));
    }

    @Test
    void rejectsOversizedResultBeforeMaterializingEveryArrayElement() {
        var outcome = executor.execute("return Array.from({ length: 1001 }, (_, index) => index);", Map.of(), 1);

        assertEquals("ERROR", outcome.status());
        assertTrue(outcome.error().contains("元素数量限制"));
    }

    @Test
    void rejectsDeepResultGraph() {
        var outcome = executor.execute(
                "let value = {}; let cursor = value; for (let index = 0; index < 30; index++) { cursor.next = {}; cursor = cursor.next; } return value;",
                Map.of(), 1);

        assertEquals("ERROR", outcome.status());
        assertTrue(outcome.error().contains("层级过深"));
    }
}
