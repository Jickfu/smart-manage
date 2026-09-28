package sm.domain.workflow.process.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.runtime.mapper.WorkflowOperationMapper;
import sm.domain.workflow.process.runtime.model.entity.WorkflowOperationEntity;
import sm.domain.workflow.process.runtime.model.form.InstanceCommandForm;
import sm.domain.workflow.process.runtime.model.form.InstanceJumpForm;
import sm.domain.workflow.process.runtime.model.form.VariableUpdateForm;
import sm.domain.workflow.process.runtime.model.form.ScriptRetryForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class RuntimeAdminTxService {
    private static final Set<String> RESERVED_VARIABLES = Set.of("formData", "orgId", "applicantId",
            "businessType", "businessId", "instanceId", "definitionId");
    private final WorkflowEngine engine;
    private final InstanceService instances;
    private final WorkflowBusinessRegistry businesses;
    private final WorkflowOperationMapper operations;
    private final RuntimeCommandWriter commands;
    private final ObjectMapper json;
    private final sm.domain.workflow.process.notification.service.NotificationService notifications;

    WorkflowEngine.Run command(String action, InstanceCommandForm form, Long actorId) {
        String digest = commands.digest(java.util.List.of(action, form));
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        var reference = lockBusiness(form.instanceId());
        var before = engine.inspect(form.instanceId());
        var result = switch (action) {
            case "SUSPEND" -> engine.suspend(form.instanceId(), actorId);
            case "RESUME" -> engine.resume(form.instanceId(), actorId);
            case "TERMINATE" -> engine.terminate(form.instanceId(), actorId, form.reason().trim());
            default -> throw new IllegalArgumentException("未知实例命令");
        };
        audit(form.instanceId(), null, action, actorId, form.reason(), before, result);
        notifications.record(reference, result);
        if (result.state() != WorkflowEngine.State.APPROVING) {
            businesses.require(reference.businessType()).completed(reference.businessId(), reference.id(),
                    sm.domain.workflow.process.runtime.contract.WorkflowBusiness.Outcome.valueOf(result.state().name()));
        }
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    WorkflowEngine.Run jump(InstanceJumpForm form, Long actorId) {
        String digest = commands.digest(form);
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        var reference = lockBusiness(form.instanceId());
        var before = engine.inspect(form.instanceId());
        var result = engine.jump(form.instanceId(), form.taskId(), actorId, form.targetNodeCode(),
                "RETURN".equals(form.action()), form.reason().trim());
        audit(form.instanceId(), form.taskId(), "RETURN".equals(form.action()) ? "ADMIN_RETURN" : "ADMIN_JUMP",
                actorId, form.reason(), before, result);
        notifications.record(reference, result);
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    WorkflowEngine.Run variables(VariableUpdateForm form, Long actorId) {
        if (form.changes().isEmpty() && form.removals().isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "至少修改一个流程变量");
        }
        if (form.changes().size() + form.removals().size() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次最多修改100个流程变量");
        }
        if (json.writeValueAsBytes(form.changes()).length > 100_000) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程变量修改内容不能超过100KB");
        }
        var keys = new java.util.LinkedHashSet<>(form.changes().keySet());
        keys.addAll(form.removals());
        if (keys.stream().anyMatch(this::invalidVariableName)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "包含不可修改的流程变量");
        }
        String digest = commands.digest(form);
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        lockBusiness(form.instanceId());
        var before = engine.inspect(form.instanceId());
        var result = engine.updateVariables(form.instanceId(), actorId, form.changes(), form.removals());
        audit(form.instanceId(), null, "VARIABLES_UPDATE", actorId, form.reason(), before.variables(), result.variables());
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    WorkflowEngine.Run retryScript(ScriptRetryForm form, Long actorId) {
        String digest = commands.digest(form);
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        var reference = lockBusiness(form.instanceId());
        var before = engine.inspect(form.instanceId());
        var result = engine.retryScript(form.instanceId(), form.taskId(), actorId);
        audit(form.instanceId(), form.taskId(), "SCRIPT_RETRY", actorId, form.reason(), before, result);
        notifications.record(reference, result);
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    private InstanceService.Reference lockBusiness(Long instanceId) {
        var reference = instances.reference(instanceId);
        businesses.require(reference.businessType()).lock(reference.businessId(), reference.id());
        return reference;
    }

    private boolean invalidVariableName(String key) {
        return key == null || !key.matches("[A-Za-z][A-Za-z0-9_.-]{0,99}")
                || key.startsWith("sm.") || key.startsWith("warm.") || RESERVED_VARIABLES.contains(key);
    }

    private void audit(Long instanceId, Long taskId, String action, Long actorId, String reason,
                       Object before, Object after) {
        var operation = new WorkflowOperationEntity();
        operation.setInstanceId(instanceId);
        operation.setTaskId(taskId);
        operation.setAction(action);
        operation.setOperatorId(actorId);
        operation.setReason(reason.trim());
        operation.setBeforeData(json.writeValueAsString(before));
        operation.setAfterData(json.writeValueAsString(after));
        operations.insert(operation);
    }
}
