package sm.domain.workflow.process.instance.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowVariableMaskerTests {
    private final WorkflowVariableMasker masker = new WorkflowVariableMasker();

    @Test
    void masksNestedSensitiveValuesAndKeepsBusinessVariables() {
        var masked = masker.mask(Map.of(
                "amount", 100,
                "accessToken", "top-secret",
                "nested", List.of(Map.of("password", 123456, "department", "研发部"))));

        assertEquals(100, masked.get("amount"));
        assertEquals("***", masked.get("accessToken"));
        var nested = (Map<?, ?>) ((List<?>) masked.get("nested")).getFirst();
        assertEquals("***", nested.get("password"));
        assertEquals("研发部", nested.get("department"));
    }
}
