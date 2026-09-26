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
        FlowEngine.userService().deleteByTaskIds(List.of(taskId));
        for (Long candidate : candidates.stream().distinct().toList()) {
            var user = FlowEngine.newUser().setType("1").setAssociated(taskId).setProcessedBy(candidate.toString()).setCreateBy(actorId.toString());
            FlowEngine.dataFillHandler().idFill(user);
            FlowEngine.userService().save(user);
        }
        return new CandidateChange(before, new Task(taskId, before.nodeCode(), before.name(), List.copyOf(candidates)));
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
    public Run inspect(Long instanceId) {
        Instance instance = FlowEngine.insService().getById(instanceId);
        if (instance == null) throw new BizException(ResultEnum.NOT_FOUND, "流程实例不存在");
        List<Task> tasks = new ArrayList<>();
        for (var task : FlowEngine.taskService().getByInsId(instanceId)) {
            List<Long> candidates = FlowEngine.userService().getPermission(task.getId(), "1").stream()
                    .map(WarmFlowRuntimeAdapter::userId).distinct().toList();
            tasks.add(new Task(task.getId(), task.getNodeCode(), task.getNodeName(), candidates));
        }
        List<History> history = new ArrayList<>();
        boolean approvalStarted = false;
        for (var entry : FlowEngine.hisTaskService().list(FlowEngine.newHisTask().setInstanceId(instanceId))) {
            boolean submitted = Objects.equals(entry.getNodeType(), 0);
            String action = submitted ? "SUBMITTED"
                    : Objects.equals(entry.getFlowStatus(), "WITHDRAWN") ? "WITHDRAWN"
                    : Objects.equals(entry.getFlowStatus(), "REJECTED") ? "REJECTED" : "APPROVED";
            if (!submitted && !action.equals("WITHDRAWN")) approvalStarted = true;
            history.add(new History(entry.getId(), entry.getNodeName(), userId(entry.getApprover()),
                    // createTime 是任务创建时间；历史完成时间由引擎写入 updateTime。
                    action, entry.getMessage(), entry.getUpdateTime() == null ? null : entry.getUpdateTime().toInstant()));
        }
        history.sort(Comparator.comparing(History::id));
        State state = Objects.equals(instance.getFlowStatus(), "WITHDRAWN") ? State.WITHDRAWN
                : Objects.equals(instance.getFlowStatus(), "REJECTED") ? State.REJECTED
                : Objects.equals(instance.getNodeType(), 2) ? State.APPROVED : State.APPROVING;
        return new Run(instance.getId(), instance.getDefinitionId(), userId(instance.getCreateBy()),
                state, List.copyOf(tasks), List.copyOf(history), approvalStarted);
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
        if (current.state() != State.APPROVING) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程已结束");
        }
        Task task = current.tasks().stream().filter(candidate -> Objects.equals(candidate.id(), taskId))
                .findFirst().orElseThrow(() -> new BizException(ResultEnum.PARAM_ERROR, "审批任务已失效，请刷新"));
        if (!task.candidates().contains(actorId)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "当前用户不能办理此任务");
        }
    }

    private static void requireCandidates(Run run) {
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

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("工作流写操作必须参与调用方事务");
        }
    }
}
