package sm.domain.workflow.process.runtime.contract;

import java.util.Map;

/** 业务单据提供的最小事务参与契约；实现不得自行开启或提交事务。 */
public interface WorkflowBusiness {
    String key();
    /** 显式关联功能目录；业务 key 不承担领域或应用归属解析职责。 */
    String featureKey();
    String name();
    Map<String, FieldType> conditionFields();
    /** 审批前锁定业务主单，并校验仍绑定指定流程轮次。锁顺序固定为主单、实例。 */
    void lock(Long businessId, Long instanceId);
    /** 当前轮次结束时回写业务状态，失败必须使引擎推进一同回滚。 */
    void completed(Long businessId, Long instanceId, Outcome outcome);
    enum Outcome { APPROVED, REJECTED, WITHDRAWN }
    enum FieldType { NUMBER, TEXT }
}
