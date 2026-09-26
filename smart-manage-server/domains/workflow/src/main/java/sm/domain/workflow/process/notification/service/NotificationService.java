package sm.domain.workflow.process.notification.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.message.inbox.contract.InboxNotificationCommand;
import sm.domain.sys.message.inbox.contract.InboxNotificationPublisher;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.notification.mapper.WorkflowOutboxMapper;
import sm.domain.workflow.process.notification.model.entity.WorkflowOutboxEntity;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final WorkflowOutboxMapper mapper;
    private final NotificationTxService transactions;
    private final InboxNotificationPublisher publisher;

    /** 只写发件箱，必须参加业务事务；消息发布在 Quartz 后续独立事务中执行。 */
    public void record(InstanceService.Reference reference, WorkflowEngine.Run run) {
        requireTransaction();
        for (var task : run.tasks()) {
            for (var candidate : task.candidates()) {
                enqueue(reference, "task:" + task.id(), candidate, "审批待办", "您有一项待处理的审批任务，请进入任务中心查看。");
            }
        }
        if (run.state() != WorkflowEngine.State.APPROVING) {
            String outcome = switch (run.state()) {
                case APPROVED -> "已通过";
                case REJECTED -> "已拒绝";
                case WITHDRAWN -> "已撤回";
                default -> throw new IllegalStateException("未知审批结果");
            };
            enqueue(reference, "result", reference.applicantId(), "审批结果", "您发起的审批" + outcome + "，请进入任务中心查看。");
        }
    }

    public void candidatesChanged(InstanceService.Reference reference, WorkflowEngine.Task task, Long changeId) {
        requireTransaction();
        for (var candidate : task.candidates()) enqueue(reference, "change:" + changeId, candidate,
                "审批待办已调整", "您被指定为当前任务的候选人，请进入任务中心查看。");
    }

    private void enqueue(InstanceService.Reference reference, String event, Long recipient, String title, String content) {
        var row = new WorkflowOutboxEntity();
        row.setId(IdWorker.getId());
        row.setEventKey(reference.id() + ":" + event + ":" + recipient);
        row.setInstanceId(reference.id());
        row.setRecipientId(recipient);
        row.setTitle(title);
        row.setContent(content);
        mapper.enqueue(row);
    }

    /** 禁止从业务事务内调用，以免消息系统暂时不可用影响审批提交。 */
    public int dispatch() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("通知投递不能参加业务事务");
        var rows = transactions.claim();
        for (var row : rows) {
            try {
                publisher.publish(new InboxNotificationCommand("workflow", row.getEventKey(), List.of(row.getRecipientId()),
                        row.getTitle(), row.getContent(), "NORMAL", LocalDateTime.now().plusDays(90),
                        "workflow.instance", row.getInstanceId().toString(), "approval", null));
                transactions.delivered(row);
            } catch (RuntimeException exception) {
                // 接收人禁用等问题保留重试和安全分类，不把异常正文或单据内容写进发件箱。
                transactions.retry(row, "DELIVERY_FAILED");
                log.warn("工作流通知等待重试: outboxId={}, errorType={}", row.getId(), exception.getClass().getSimpleName());
            }
        }
        return rows.size();
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("工作流通知必须与业务一同提交");
    }
}
