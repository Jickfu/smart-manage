package sm.domain.demo.office.leave.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.demo.office.leave.mapper.LeaveMapper;
import sm.domain.demo.office.leave.model.entity.LeaveEntity;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.util.Objects;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LeaveWorkflowParticipant implements WorkflowBusiness {
    private final LeaveMapper mapper;
    @Override public String key() { return LeaveResourceRegistration.BUSINESS_TYPE; }
    @Override public String featureKey() { return "demo/office/leave"; }
    @Override public String name() { return "请假申请"; }
    @Override public Map<String, FieldType> conditionFields() { return Map.of("days", FieldType.NUMBER, "leaveType", FieldType.TEXT); }
    @Override
    public void lock(Long businessId, Long instanceId) { requireActive(businessId, instanceId); }
    @Override
    public void completed(Long businessId, Long instanceId, Outcome outcome) {
        var entity = requireActive(businessId, instanceId);
        entity.setBillStatus(outcome == Outcome.APPROVED ? "C" : "A");
        entity.setLastOutcome(outcome.name());
        if (mapper.updateById(entity) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "请假状态已变化");
    }
    private LeaveEntity requireActive(Long id, Long instanceId) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("请假审批回写必须参与工作流事务");
        var entity = mapper.selectOne(new LambdaQueryWrapper<LeaveEntity>().eq(LeaveEntity::getId, id).last("FOR UPDATE"));
        if (entity == null || !"B".equals(entity.getBillStatus()) || !Objects.equals(instanceId, entity.getCurrentInstanceId())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "该轮次已不是请假单的当前审批");
        }
        return entity;
    }
}
