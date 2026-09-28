package sm.domain.workflow.process.script.service;

import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.entity.Node;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.listener.GlobalListener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.script.mapper.WorkflowScriptExecutionMapper;
import sm.domain.workflow.process.script.model.entity.WorkflowScriptExecutionEntity;
import sm.system.script.RestrictedScriptExecutor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 Smart Manage SCRIPT 逻辑节点映射到 Warm-Flow 中间节点的创建事件。 */
@Component("workflowScriptListener")
@RequiredArgsConstructor
public class WorkflowScriptListener implements GlobalListener, SmartInitializingSingleton {
    private static final java.util.Set<String> RESERVED = java.util.Set.of("formData", "orgId", "applicantId",
            "businessType", "businessId", "instanceId", "definitionId");
    private final RestrictedScriptExecutor executor;
    private final WorkflowScriptExecutionMapper executions;
    private final UserReferenceReader users;
    private final ObjectMapper json;

    @Override
    public void afterSingletonsInstantiated() {
        // 当前领域是唯一 Warm-Flow 装配者；通过稳定 Bean 名注册，第三方监听器类型不离开适配层。
        FlowEngine.initGlobalListener("workflowScriptListener");
    }

    @Override
    public void create(ListenerVariable variable) {
        if (variable.getNode() == null || variable.getTask() == null || !isScript(variable.getNode())) return;
        Instance instance = variable.getInstance();
        Long actorId = positiveLong(instance.getCreateBy());
        execute(instance, variable.getNode(), variable.getTask(), actorId, false);
    }

    public boolean retry(Long instanceId, Long taskId, Long operatorId) {
        Instance instance = FlowEngine.insService().getById(instanceId);
        Task task = FlowEngine.taskService().getById(taskId);
        if (instance == null || task == null || !instanceId.equals(task.getInstanceId())) return false;
        Node node = FlowEngine.nodeService().getByDefIdAndNodeCode(instance.getDefinitionId(), task.getNodeCode());
        if (node == null || !isScript(node)) return false;
        if (!FlowEngine.insService().active(instanceId)) return false;
        return execute(FlowEngine.insService().getById(instanceId), node, task, operatorId, true);
    }

    private boolean execute(Instance instance, Node node, Task task, Long operatorId, boolean retry) {
        Map<String, Object> extension = extension(node);
        String source = String.valueOf(extension.getOrDefault("script", ""));
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("instanceId", instance.getId().toString());
        context.put("definitionId", instance.getDefinitionId().toString());
        context.put("nodeCode", node.getNodeCode());
        context.put("variables", instance.getVariableMap() == null ? Map.of() : instance.getVariableMap());
        var outcome = executor.execute(source, context, 3);
        var execution = new WorkflowScriptExecutionEntity();
        execution.setInstanceId(instance.getId());
        execution.setTaskId(task.getId());
        execution.setNodeCode(node.getNodeCode());
        execution.setStatus(outcome.status());
        execution.setDurationMs(outcome.durationMs());
        execution.setOperatorId(operatorId);
        if (!"SUCCESS".equals(outcome.status())) {
            execution.setErrorMessage(outcome.error());
            executions.insert(execution);
            FlowEngine.insService().unActive(instance.getId());
            return false;
        }
        try {
            ScriptResult result = validate(outcome.value());
            execution.setResultData(json.writeValueAsString(result));
            executions.insert(execution);
            if (!result.variables().isEmpty()) {
                FlowEngine.taskService().mergeVariable(instance, result.variables());
                FlowEngine.insService().updateById(instance);
            }
            String actor = operatorId.toString();
            FlowParams params = FlowParams.build().handler(actor).permissionFlag(List.of(actor)).ignore(true)
                    .skipType("PASS").flowStatus("SCRIPT").hisStatus("SCRIPT")
                    .message(retry ? "管理员重试脚本节点" : "脚本节点自动完成")
                    .variable(result.variables());
            if (!result.participants().isEmpty()) {
                params.nextHandler(result.participants().stream().map(String::valueOf).toArray(String[]::new));
            }
            FlowEngine.taskService().skip(task.getId(), params);
            return true;
        } catch (RuntimeException failure) {
            execution.setStatus("ERROR");
            execution.setErrorMessage(safeMessage(failure));
            if (execution.getId() == null) executions.insert(execution); else executions.updateById(execution);
            FlowEngine.insService().unActive(instance.getId());
            return false;
        }
    }

    private ScriptResult validate(Object value) {
        if (!(value instanceof Map<?, ?> raw)) throw new IllegalArgumentException("脚本必须返回对象");
        Map<String, Object> variables = new LinkedHashMap<>();
        Object variableValue = raw.get("variables");
        if (variableValue != null) {
            if (!(variableValue instanceof Map<?, ?> values) || values.size() > 100) {
                throw new IllegalArgumentException("variables 必须是最多100项的对象");
            }
            for (var entry : values.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (!key.matches("[A-Za-z][A-Za-z0-9_.-]{0,99}") || key.startsWith("sm.")
                        || key.startsWith("warm.") || RESERVED.contains(key)) {
                    throw new IllegalArgumentException("脚本返回了不可修改的流程变量");
                }
                variables.put(key, entry.getValue());
            }
        }
        List<Long> participants = List.of();
        Object participantValue = raw.get("participants");
        if (participantValue != null) {
            if (!(participantValue instanceof List<?> values) || values.isEmpty() || values.size() > 100) {
                throw new IllegalArgumentException("participants 必须是1到100名用户");
            }
            participants = values.stream().map(valueId -> {
                if (!(valueId instanceof Number number) || number.longValue() <= 0) {
                    throw new IllegalArgumentException("participants 包含无效用户");
                }
                return number.longValue();
            }).distinct().toList();
            users.requireEnabledByIds(participants);
        }
        if (json.writeValueAsBytes(variables).length > 100_000) throw new IllegalArgumentException("脚本变量结果过大");
        return new ScriptResult(variables, participants);
    }

    public boolean isScript(Node node) {
        if (node.getExt() == null || node.getExt().isBlank()) return false;
        try {
            return "SCRIPT".equals(extension(node).get("smNodeType"));
        } catch (RuntimeException failure) {
            return false;
        }
    }

    private Map<String, Object> extension(Node node) {
        JsonNode value = json.readTree(node.getExt());
        Map<String, Object> result = new LinkedHashMap<>();
        if (value.isObject()) {
            value.properties().forEach(entry -> result.put(entry.getKey(), json.treeToValue(entry.getValue(), Object.class)));
        } else if (value.isArray()) {
            value.forEach(row -> {
                if (row.hasNonNull("code")) result.put(row.path("code").asText(),
                        json.treeToValue(row.get("value"), Object.class));
            });
        }
        return result;
    }

    private static Long positiveLong(String value) {
        long parsed = Long.parseLong(value);
        if (parsed <= 0) throw new IllegalArgumentException("流程申请人无效");
        return parsed;
    }

    private static String safeMessage(Throwable failure) {
        String message = failure.getMessage();
        if (message == null || message.isBlank()) return failure.getClass().getSimpleName();
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    public record ScriptResult(Map<String, Object> variables, List<Long> participants) { }
}
