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
    Run inspect(Long instanceId);
    Selection select(Long actorId, Box box, int offset, int limit);
    String chart(Long instanceId);
    CandidateChange replaceCandidates(Long instanceId, Long taskId, Long actorId, List<Long> candidates);
    record CandidateChange(Task before, Task after) { }

    enum Box { PENDING, COMPLETED, STARTED }
    record Selection(List<Long> instanceIds, long total) { }

    enum State { APPROVING, APPROVED, REJECTED, WITHDRAWN }
    record Task(Long id, String nodeCode, String name, List<Long> candidates) {}
    record History(Long id, String nodeName, Long actorId, String action, String opinion, Instant time) {}
    record Run(Long id, Long definitionId, Long applicantId, State state, List<Task> tasks,
               List<History> history, boolean approvalStarted) {}
}
