package sm.domain.workflow.process.engine.warmflow.helper;
import org.dromara.warm.flow.core.dto.*;
import org.junit.jupiter.api.Test;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;
import sm.system.exception.BizException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WarmFlowDefinitionPolicyTests {
    private static final Map<String, FieldType> FIELDS = Map.of("days", FieldType.NUMBER, "leaveType", FieldType.TEXT);
    @Test
    void publishesOnlyDisjointNumericAndTextConditionsIncludingBoundaryEquality() {
        assertDoesNotThrow(() -> validate("lt@@days|3", "ge@@days|3"));
        assertDoesNotThrow(() -> validate("eq@@days|3.00", "ne@@days|3"));
        assertDoesNotThrow(() -> validate("eq@@leaveType|SICK", "eq@@leaveType|ANNUAL"));
        for (String[] pair : List.of(new String[]{"le@@days|3", "ge@@days|3"}, new String[]{"ge@@days|3", "gt@@days|5"},
                new String[]{"eq@@days|3.00", "eq@@days|3"}, new String[]{"ne@@days|3", "ne@@days|5"},
                new String[]{"eq@@days|3", "eq@@leaveType|SICK"}, new String[]{"ne@@leaveType|SICK", "eq@@leaveType|ANNUAL"})) {
            assertThrows(BizException.class, () -> validate(pair[0], pair[1]), Arrays.toString(pair));
        }
    }
    @Test
    void rejectsScriptUnknownFieldsWrongTypesAndDangerousNodeCapabilities() {
        for (String expression : List.of("${T(java.lang.Runtime).getRuntime()}", "spel@@true", "eq@@secret|1", "gt@@leaveType|3", "eq@@days|NaN")) {
            var definition = graph(expression, null);
            assertThrows(BizException.class, () -> WarmFlowDefinitionPolicy.validate(definition, FIELDS, true), expression);
        }
        var listener = graph("gt@@days|3", null);
        listener.getNodeList().get(2).setListenerPath("bean:danger");
        assertThrows(BizException.class, () -> WarmFlowDefinitionPolicy.validate(listener, FIELDS, false));
        var startExpression = graph("gt@@days|3", null);
        startExpression.getNodeList().getFirst().setPermissionFlag("spel@@danger");
        assertThrows(BizException.class, () -> WarmFlowDefinitionPolicy.validate(startExpression, FIELDS, true));
    }
    @Test
    void incompleteDraftCanSaveButCannotPublishAndLoopsAreRejected() {
        var draft = graph("gt@@days|3", null);
        draft.getNodeList().get(2).setPermissionFlag(null);
        assertDoesNotThrow(() -> WarmFlowDefinitionPolicy.validate(draft, FIELDS, false));
        assertThrows(BizException.class, () -> WarmFlowDefinitionPolicy.validate(draft, FIELDS, true));
        var loop = graph("gt@@days|3", null);
        loop.getNodeList().get(2).getSkipList().getFirst().setNextNodeCode("gate");
        assertThrows(BizException.class, () -> WarmFlowDefinitionPolicy.validate(loop, FIELDS, true));
    }
    private void validate(String left, String right) { WarmFlowDefinitionPolicy.validate(graph(left, right), FIELDS, true); }
    private DefJson graph(String left, String right) {
        var branches = new ArrayList<SkipJson>();
        branches.add(edge("gate", "review", left));
        if (right != null) branches.add(edge("gate", "other", right));
        branches.add(edge("gate", "fallback", null));
        var nodes = new ArrayList<>(List.of(node("start", 0, "gate"), new NodeJson().setNodeCode("gate").setNodeType(3).setSkipList(branches),
                node("review", 1, "end"), node("fallback", 1, "end"), node("end", 2, null)));
        if (right != null) nodes.add(node("other", 1, "end"));
        return new DefJson().setModelValue("CLASSICS").setNodeList(nodes);
    }
    private NodeJson node(String code, int type, String next) { return new NodeJson().setNodeCode(code).setNodeType(type).setNodeRatio("0")
            .setPermissionFlag(type == 1 ? "sm:user:20" : null).setSkipList(next == null ? List.of() : List.of(edge(code, next, null))); }
    private SkipJson edge(String from, String to, String condition) { return new SkipJson().setNowNodeCode(from).setNextNodeCode(to).setSkipType("PASS").setSkipCondition(condition); }
}
