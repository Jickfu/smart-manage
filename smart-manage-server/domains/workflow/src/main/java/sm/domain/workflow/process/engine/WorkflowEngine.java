package sm.domain.workflow.process.engine;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** 工作流运行边界；引擎类型不进入业务单据和任务界面。 */
public interface WorkflowEngine {
    Run start(String flowCode, String businessReference, Long applicantId, Map<String, Object> variables);
    Run approve(Long instanceId, Long taskId, Long actorId, String opinion);
    Run reject(Long instanceId, Long taskId, Long actorId, String opinion);
    Run withdraw(Long instanceId, Long actorId);
    Run suspend(Long instanceId, Long actorId);
    Run resume(Long instanceId, Long actorId);
    Run terminate(Long instanceId, Long actorId, String reason);
    Run jump(Long instanceId, Long taskId, Long actorId, String targetNodeCode, boolean backward, String reason);
    Run updateVariables(Long instanceId, Long actorId, Map<String, Object> changes, List<String> removals);
    Run cooperate(Long instanceId, Long taskId, Long actorId, Cooperation action,
                  List<Long> targetUserIds, String reason);
    Run takeBack(Long instanceId, Long actorId, String reason);
    Run retryScript(Long instanceId, Long taskId, Long actorId);
    Run inspect(Long instanceId);
    Selection select(Long actorId, Box box, int offset, int limit);
    String chart(Long instanceId);
    CandidateChange replaceCandidates(Long instanceId, Long taskId, Long actorId, List<Long> candidates);
    record CandidateChange(Task before, Task after) { }

    enum Box { PENDING, COMPLETED, STARTED }
    record Selection(List<Long> instanceIds, long total) { }

    enum State { APPROVING, APPROVED, REJECTED, WITHDRAWN, TERMINATED }
    enum Cooperation { TRANSFER, DELEGATE, ADD_SIGN, REDUCE_SIGN }
    enum HistoryCategory {
        APPROVAL,
        COOPERATION,
        MANAGEMENT,
        SCRIPT;

        /** 只有真实参与过业务审批或任务协作的人，才能据此取得业务单据读取资格。 */
        public boolean grantsParticipation() {
            return this == APPROVAL || this == COOPERATION;
        }
    }
    record Task(Long id, String nodeCode, String name, List<Long> candidates, boolean script) {}
    record NodeTarget(String code, String name, boolean script) {}
    record History(Long id, String nodeCode, String nodeName, Long actorId, String action,
                   String opinion, Instant time, HistoryCategory category) {}
    record Run(Long id, Long definitionId, Long applicantId, State state, List<Task> tasks,
               List<History> history, List<NodeTarget> nodeTargets, boolean approvalStarted,
               boolean active, Map<String, Object> variables) {}
}
