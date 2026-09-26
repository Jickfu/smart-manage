package sm.domain.workflow.process.runtime.contract;

import java.util.Map;
import java.util.UUID;

/** 调用方在锁定业务单据后提交，组织、申请人和快照都来自服务端已校验的单据。 */
public interface WorkflowSubmission {
    Long submit(Command command);
    /** 仅返回当前申请人的幂等提交结果；没有已提交结果时返回 null。 */
    Receipt findSubmission(UUID requestId, String requestDigest);
    record Receipt(String businessType, Long businessId, Long instanceId) { }
    record Command(String businessType, Long businessId, String number, Long orgId, Long applicantId,
                   UUID requestId, String requestDigest, String snapshot, Map<String, Object> variables) { }
}
