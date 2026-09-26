package sm.domain.workflow.process.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.workflow.process.notification.mapper.WorkflowOutboxMapper;
import sm.domain.workflow.process.notification.model.entity.WorkflowOutboxEntity;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class NotificationTxService {
    private final WorkflowOutboxMapper mapper;
    List<WorkflowOutboxEntity> claim() { return mapper.claim(UUID.randomUUID()); }
    void delivered(WorkflowOutboxEntity row) { mapper.delivered(row.getId(), row.getClaimToken()); }
    void retry(WorkflowOutboxEntity row, String reason) { mapper.retry(row.getId(), row.getClaimToken(), reason); }
}
