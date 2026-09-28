package sm.domain.workflow.process.engine.warmflow.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.orm.entity.FlowInstance;
import org.dromara.warm.flow.orm.mapper.FlowInstanceMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowTaskQueryMapper;
import org.dromara.warm.flow.core.dto.DefJson;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 真实引擎操作与实例行锁集中在适配器内；外层事务仍由业务编排拥有。 */
@Component
@RequiredArgsConstructor
public class WarmFlowRuntimeAdapter implements WorkflowEngine {
    private final FlowInstanceMapper instanceMapper;
    private final WarmFlowTaskQueryMapper taskQueries;
    private final sm.domain.workflow.process.script.service.WorkflowScriptListener scripts;

    @Override
    public CandidateChange replaceCandidates(Long instanceId, Long taskId, Long actorId, List<Long> candidates) {
        lock(instanceId);
        Run run = inspect(instanceId);
        Task before = run.tasks().stream().filter(task -> Objects.equals(task.id(), taskId)).findFirst()
                .orElseThrow(() -> new BizException(ResultEnum.DATA_CONFLICT, "当前任务已结束，请刷新"));
        if (candidates == null || candidates.isEmpty() || candidates.size() > 100 || candidates.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "必须提供有效的候选人");
        }
        // 这是资格维护，不推进节点，也不向审批历史插入伪造的审批记录。
        List<Long> distinctCandidates = candidates.stream().distinct().toList();
        replaceTaskCandidates(taskId, distinctCandidates, actorId);
        return new CandidateChange(before, new Task(taskId, before.nodeCode(), before.name(), distinctCandidates, before.script()));
    }

    @Override
    public Selection select(Long actorId, Box box, int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 100) throw new BizException(ResultEnum.PARAM_ERROR, "分页参数无效");
        return new Selection(taskQueries.selectInstances(actorId.toString(), box, offset, limit), taskQueries.countInstances(actorId.toString(), box));
    }

    @Override
    public String chart(Long instanceId) {
        var instance = FlowEngine.insService().getById(instanceId);
        if (instance == null) throw new BizException(ResultEnum.NOT_FOUND, "流程实例不存在");
        var chart = FlowEngine.jsonConvert.strToBean(instance.getDefJson(), DefJson.class);
        // 历史图使用发起时保存的定义；不把实例变量或引擎人员表直接暴露给设计器。
        chart.setChartStatusColor(FlowEngine.chartService().getChartRgb(chart.getModelValue()));
        return FlowEngine.jsonConvert.objToStr(chart);
    }

    @Override
    public Run start(String flowCode, String businessReference, Long applicantId, Map<String, Object> variables) {
        requireTransaction();
        Instance instance = FlowEngine.insService().start(businessReference,
                parameters(applicantId, null).flowCode(flowCode).variable(variables));
        Run result = inspect(instance.getId());
        requireCandidates(result);
        return result;
    }

    @Override
    public Run approve(Long instanceId, Long taskId, Long actorId, String opinion) {
        lock(instanceId);
        requireTask(instanceId, taskId, actorId);
        FlowEngine.taskService().skip(taskId, parameters(actorId, opinion).skipType("PASS"));
        Run result = inspect(instanceId);
        // 空候选会导致原节点审批也回滚，不能提交后留下无人可办的实例。
        requireCandidates(result);
        return result;
    }

    @Override
    public Run reject(Long instanceId, Long taskId, Long actorId, String opinion) {
        lock(instanceId);
        requireTask(instanceId, taskId, actorId);
        // 上游 reject 是退回；产品的“拒绝”必须终止本轮，历史结果单独标识。
        FlowEngine.taskService().termination(taskId,
                parameters(actorId, opinion).flowStatus("REJECTED").hisStatus("REJECTED"));
        return inspect(instanceId);
    }

    @Override
    public Run withdraw(Long instanceId, Long actorId) {
        lock(instanceId);
        Run before = inspect(instanceId);
        if (!Objects.equals(before.applicantId(), actorId)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "只有申请人可以撤回流程");
        }
        if (before.state() != State.APPROVING || before.approvalStarted()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程已有审批记录或已结束，不能撤回");
        }
        // 已在同一行锁内验证申请人、未审批和运行态；申请人不必是当前节点候选人。
        FlowEngine.taskService().terminationByInsId(instanceId,
                parameters(actorId, "申请人撤回").flowStatus("WITHDRAWN").hisStatus("WITHDRAWN").ignore(true));
        return inspect(instanceId);
    }

    @Override
    public Run suspend(Long instanceId, Long actorId) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (!before.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "流程已经挂起");
        if (!FlowEngine.insService().unActive(instanceId)) throw new BizException(ResultEnum.DATA_CONFLICT, "流程挂起失败");
        return inspect(instanceId);
    }

    @Override
    public Run resume(Long instanceId, Long actorId) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (before.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "流程已经处于活动状态");
        if (!FlowEngine.insService().active(instanceId)) throw new BizException(ResultEnum.DATA_CONFLICT, "流程恢复失败");
        return inspect(instanceId);
    }

    @Override
    public Run terminate(Long instanceId, Long actorId, String reason) {
        lock(instanceId);
        requireRunning(inspect(instanceId));
        FlowEngine.taskService().terminationByInsId(instanceId,
                parameters(actorId, reason).flowStatus("TERMINATED").hisStatus("TERMINATED").ignore(true));
        return inspect(instanceId);
    }

    @Override
    public Run jump(Long instanceId, Long taskId, Long actorId, String targetNodeCode, boolean backward, String reason) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (!before.tasks().stream().anyMatch(task -> Objects.equals(task.id(), taskId))) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "当前任务已结束，请刷新");
        }
        if (before.tasks().stream().anyMatch(task -> Objects.equals(task.id(), taskId)
                && Objects.equals(task.nodeCode(), targetNodeCode))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "目标节点不能是当前节点");
        }
        if (before.nodeTargets().stream().noneMatch(target -> Objects.equals(target.code(), targetNodeCode))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "目标节点不存在或不允许作为跳转目标");
        }
        boolean visited = before.history().stream().anyMatch(history -> Objects.equals(history.nodeCode(), targetNodeCode));
        if (backward != visited) {
            throw new BizException(ResultEnum.PARAM_ERROR, backward ? "退回只能选择已流转节点" : "向后跳转不能选择已流转节点");
        }
        FlowParams params = parameters(actorId, reason).nodeCode(targetNodeCode).ignore(true)
                .flowStatus(backward ? "ADMIN_RETURN" : "ADMIN_JUMP")
                .hisStatus(backward ? "ADMIN_RETURN" : "ADMIN_JUMP")
                .skipType(backward ? "REJECT" : "PASS");
        FlowEngine.taskService().skip(taskId, params);
        Run result = inspect(instanceId);
        requireCandidates(result);
        return result;
    }

    @Override
    public Run updateVariables(Long instanceId, Long actorId, Map<String, Object> changes, List<String> removals) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (before.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "请先挂起流程再修改变量");
        Instance instance = FlowEngine.insService().getById(instanceId);
        if (removals != null && !removals.isEmpty()) {
            FlowEngine.insService().removeVariables(instanceId, removals.toArray(String[]::new));
            instance = FlowEngine.insService().getById(instanceId);
        }
        if (changes != null && !changes.isEmpty()) {
            FlowEngine.taskService().mergeVariable(instance, changes);
            if (!FlowEngine.insService().updateById(instance)) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "流程变量修改失败");
            }
        }
        return inspect(instanceId);
    }

    @Override
    public Run cooperate(Long instanceId, Long taskId, Long actorId, Cooperation action,
                         List<Long> targetUserIds, String reason) {
        lock(instanceId);
        requireTask(instanceId, taskId, actorId);
        List<String> targets = targetUserIds.stream().distinct().map(String::valueOf).toList();
        FlowParams params = parameters(actorId, reason);
        boolean changed = switch (action) {
            case TRANSFER -> FlowEngine.taskService().transfer(taskId, params.addHandlers(targets));
            case DELEGATE -> FlowEngine.taskService().depute(taskId, params.addHandlers(targets));
            case ADD_SIGN -> FlowEngine.taskService().addSignature(taskId, params.addHandlers(targets));
            case REDUCE_SIGN -> FlowEngine.taskService().reductionSignature(taskId, params.reductionHandlers(targets));
        };
        if (!changed) throw new BizException(ResultEnum.DATA_CONFLICT, "任务协作操作未生效，请刷新");
        return inspect(instanceId);
    }

    @Override
    public Run takeBack(Long instanceId, Long actorId, String reason) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (!before.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "流程已挂起");
        // 上游按当前办理人最近一次已办记录定位拿回节点，并校验其确实办理过该流程。
        FlowEngine.taskService().taskBackByInsId(instanceId,
                parameters(actorId, reason).flowStatus("TASK_BACK"));
        Run result = inspect(instanceId);
        // 上游按定义重新解析退回节点人员；“拿回”产品语义要求任务回到实际已办人手中。
        for (Task task : result.tasks()) replaceTaskCandidates(task.id(), List.of(actorId), actorId);
        result = inspect(instanceId);
        requireCandidates(result);
        return result;
    }

    @Override
    public Run retryScript(Long instanceId, Long taskId, Long actorId) {
        lock(instanceId);
        Run before = inspect(instanceId);
        requireRunning(before);
        if (before.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "仅能重试因脚本失败而挂起的实例");
        if (before.tasks().stream().noneMatch(task -> Objects.equals(task.id(), taskId) && task.script())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "当前任务不是可重试的脚本节点");
        }
        scripts.retry(instanceId, taskId, actorId);
        Run result = inspect(instanceId);
        requireCandidates(result);
        return result;
    }

    @Override
    public Run inspect(Long instanceId) {
        Instance instance = FlowEngine.insService().getById(instanceId);
        if (instance == null) throw new BizException(ResultEnum.NOT_FOUND, "流程实例不存在");
        List<Task> tasks = new ArrayList<>();
        for (var task : FlowEngine.taskService().getByInsId(instanceId)) {
            // 转办和委派使用 Warm-Flow 独立人员类型，它们同样是当前任务的真实办理资格。
            List<Long> candidates = FlowEngine.userService().getPermission(task.getId(), "1", "2", "3").stream()
                    .map(WarmFlowRuntimeAdapter::userId).distinct().toList();
            var node = FlowEngine.nodeService().getByDefIdAndNodeCode(instance.getDefinitionId(), task.getNodeCode());
            tasks.add(new Task(task.getId(), task.getNodeCode(), task.getNodeName(), candidates,
                    node != null && scripts.isScript(node)));
        }
        List<History> history = new ArrayList<>();
        boolean approvalStarted = false;
        for (var entry : FlowEngine.hisTaskService().list(FlowEngine.newHisTask().setInstanceId(instanceId))) {
            boolean submitted = Objects.equals(entry.getNodeType(), 0);
            String action = submitted ? "SUBMITTED"
                    : Objects.equals(entry.getFlowStatus(), "WITHDRAWN") ? "WITHDRAWN"
                    : Objects.equals(entry.getFlowStatus(), "REJECTED") ? "REJECTED" : "APPROVED";
            if (Objects.equals(entry.getFlowStatus(), "SCRIPT")) action = "SCRIPT";
            if (Objects.equals(entry.getFlowStatus(), "TERMINATED")) action = "TERMINATED";
            if (Objects.equals(entry.getFlowStatus(), "TASK_BACK")) action = "TASK_BACK";
            if (Objects.equals(entry.getFlowStatus(), "ADMIN_JUMP")) action = "ADMIN_JUMP";
            if (Objects.equals(entry.getFlowStatus(), "ADMIN_RETURN")) action = "ADMIN_RETURN";
            if (Objects.equals(entry.getCooperateType(), 2)) action = "TRANSFER";
            if (Objects.equals(entry.getCooperateType(), 3)) action = "DELEGATE";
            if (Objects.equals(entry.getCooperateType(), 6)) action = "ADD_SIGN";
            if (Objects.equals(entry.getCooperateType(), 7)) action = "REDUCE_SIGN";
            if ("APPROVED".equals(action) || "REJECTED".equals(action)) approvalStarted = true;
            history.add(new History(entry.getId(), entry.getNodeCode(), entry.getNodeName(), userId(entry.getApprover()),
                    // createTime 是任务创建时间；历史完成时间由引擎写入 updateTime。
                    action, entry.getMessage(), entry.getUpdateTime() == null ? null : entry.getUpdateTime().toInstant(),
                    historyCategory(action)));
        }
        // 脚本节点可在 Warm-Flow 写入开始历史前同步完成；产品轨迹仍应从“提交”开始。
        history.sort(Comparator.comparing((History item) -> !"SUBMITTED".equals(item.action()))
                .thenComparing(History::id));
        State state = Objects.equals(instance.getFlowStatus(), "WITHDRAWN") ? State.WITHDRAWN
                : Objects.equals(instance.getFlowStatus(), "REJECTED") ? State.REJECTED
                : Objects.equals(instance.getFlowStatus(), "TERMINATED") ? State.TERMINATED
                : Objects.equals(instance.getNodeType(), 2) ? State.APPROVED : State.APPROVING;
        Map<String, Object> variables = instance.getVariableMap() == null
                ? Map.of() : new java.util.LinkedHashMap<>(instance.getVariableMap());
        DefJson definition = FlowEngine.jsonConvert.strToBean(instance.getDefJson(), DefJson.class);
        List<NodeTarget> nodeTargets = definition.getNodeList().stream()
                .filter(node -> Objects.equals(node.getNodeType(), 1))
                .map(node -> new NodeTarget(node.getNodeCode(), node.getNodeName(), scriptNode(node.getExt())))
                .toList();
        return new Run(instance.getId(), instance.getDefinitionId(), userId(instance.getCreateBy()),
                state, List.copyOf(tasks), List.copyOf(history), nodeTargets, approvalStarted,
                Objects.equals(instance.getActivityStatus(), 1),
                java.util.Collections.unmodifiableMap(variables));
    }

    private static HistoryCategory historyCategory(String action) {
        return switch (action) {
            case "TRANSFER", "DELEGATE", "ADD_SIGN", "REDUCE_SIGN", "TASK_BACK" ->
                    HistoryCategory.COOPERATION;
            case "TERMINATED", "ADMIN_JUMP", "ADMIN_RETURN" -> HistoryCategory.MANAGEMENT;
            case "SCRIPT" -> HistoryCategory.SCRIPT;
            default -> HistoryCategory.APPROVAL;
        };
    }

    private void lock(Long instanceId) {
        requireTransaction();
        if (instanceMapper.selectOne(new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getId, instanceId).last("FOR UPDATE")) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "流程实例不存在");
        }
    }

    private void requireTask(Long instanceId, Long taskId, Long actorId) {
        Run current = inspect(instanceId);
        if (!current.active()) throw new BizException(ResultEnum.DATA_CONFLICT, "流程已挂起");
        if (current.state() != State.APPROVING) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程已结束");
        }
        Task task = current.tasks().stream().filter(candidate -> Objects.equals(candidate.id(), taskId))
                .findFirst().orElseThrow(() -> new BizException(ResultEnum.PARAM_ERROR, "审批任务已失效，请刷新"));
        if (!task.candidates().contains(actorId)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "当前用户不能办理此任务");
        }
    }

    private static void requireRunning(Run run) {
        if (run.state() != State.APPROVING) throw new BizException(ResultEnum.PARAM_ERROR, "流程已结束");
    }

    private static void requireCandidates(Run run) {
        if (!run.active()) return;
        for (Task task : run.tasks()) {
            if (task.candidates().isEmpty()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "节点“" + task.name() + "”没有有效审批人");
            }
        }
    }

    private static FlowParams parameters(Long actorId, String opinion) {
        if (actorId == null || actorId <= 0) throw new BizException(ResultEnum.PARAM_ERROR, "审批主体无效");
        return FlowParams.build().handler(actorId.toString()).permissionFlag(List.of(actorId.toString())).message(opinion);
    }

    private static Long userId(String value) {
        try {
            long identifier = Long.parseLong(value);
            if (identifier <= 0) throw new NumberFormatException();
            return identifier;
        } catch (RuntimeException failure) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程人员标识不符合本项目约定");
        }
    }

    private boolean scriptNode(String extension) {
        if (extension == null || extension.isBlank()) return false;
        var node = FlowEngine.newNode().setExt(extension);
        return scripts.isScript(node);
    }

    private static void replaceTaskCandidates(Long taskId, List<Long> candidates, Long actorId) {
        FlowEngine.userService().deleteByTaskIds(List.of(taskId));
        for (Long candidate : candidates) {
            var user = FlowEngine.newUser().setType("1").setAssociated(taskId)
                    .setProcessedBy(candidate.toString()).setCreateBy(actorId.toString());
            FlowEngine.dataFillHandler().idFill(user);
            FlowEngine.userService().save(user);
        }
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("工作流写操作必须参与调用方事务");
        }
    }
}
