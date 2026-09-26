package sm.domain.workflow.process.instance.contract;
/** 读取指定历史轮次的不可变快照；实现基于当前登录人核对参与资格。 */
public interface WorkflowHistoryReader {
    String requireSnapshot(String businessType, Long businessId, Long instanceId);
    boolean canRead(String businessType, Long businessId, Long instanceId);
}
