package sm.domain.workflow.process.instance.contract;

import sm.domain.workflow.process.engine.WorkflowEngine;

import java.time.LocalDateTime;
import java.util.List;

/** 读取历史审批轮次；参与人入口由本实现鉴权，业务入口必须先完成业务权限与 DataScope 校验。 */
public interface WorkflowHistoryReader {
    String requireSnapshot(String businessType, Long businessId, Long instanceId);
    boolean canRead(String businessType, Long businessId, Long instanceId);
    List<Round> listBusinessRounds(String businessType, Long businessId);
    String requireBusinessSnapshot(String businessType, Long businessId, Long instanceId);

    record Round(Long instanceId, Long definitionId, WorkflowEngine.State state, boolean active,
                 String currentNode, LocalDateTime createTime) { }
}
