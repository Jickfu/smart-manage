package sm.domain.sys.message.inbox.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.message.inbox.mapper.InboxMessageMapper;
import sm.domain.sys.message.inbox.model.entity.InboxMessageEntity;
import sm.domain.sys.message.inbox.model.form.InboxMessageListForm;
import sm.domain.sys.message.inbox.model.form.InboxMessageSaveForm;
import sm.domain.sys.message.inbox.model.form.InboxMessageVersionForm;
import sm.domain.sys.message.inbox.model.vo.InboxMessageCreateNewDataVO;
import sm.domain.sys.message.inbox.model.vo.InboxMessageDetailVO;
import sm.domain.sys.message.inbox.model.vo.InboxMessageListVO;
import sm.system.aop.log.BizLog;
import sm.system.query.ListSqlQuery;
import sm.system.response.PageData;
import sm.system.security.authorization.AdministratorOnly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageAdminService {
    private static final Set<String> STATUSES = Set.of("DRAFT", "PENDING", "PUBLISHING", "PUBLISHED", "FAILED");
    private static final Set<String> LEVELS = Set.of("NORMAL", "IMPORTANT", "URGENT");
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "title", ListSqlQuery.string("a.title", true),
            "level", ListSqlQuery.enumeration("a.level", true),
            "status", ListSqlQuery.enumeration("a.status", true),
            "senderName", ListSqlQuery.string("a.sender_name", true),
            "recipientCount", ListSqlQuery.number("a.recipient_count", true),
            "publishTime", ListSqlQuery.dateTime("a.publish_time", true),
            "expireTime", ListSqlQuery.dateTime("a.expire_time", true),
            "createTime", ListSqlQuery.dateTime("a.create_time", true));

    private final InboxMessageMapper messageMapper;
    private final InboxMessageTxService txService;

    public PageData<InboxMessageListVO> listPage(InboxMessageListForm form) {
        validateOptional(form.getStatus(), STATUSES, "消息状态不合法");
        validateOptional(form.getLevel(), LEVELS, "消息级别不合法");
        Page<InboxMessageListVO> page = messageMapper.selectAdminListPage(
                new Page<>(form.getPageNum(), form.getPageSize()), form, ListSqlQuery.of(form, LIST_FIELDS));
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), page.getRecords());
    }

    public InboxMessageDetailVO detail(Long id) {
        InboxMessageEntity entity = txService.requireMessage(id);
        InboxMessageDetailVO result = new InboxMessageDetailVO();
        result.setId(entity.getId()); result.setVersion(entity.getVersion()); result.setTitle(entity.getTitle());
        result.setContent(entity.getContent()); result.setLevel(entity.getLevel()); result.setStatus(entity.getStatus());
        result.setSenderName(entity.getSenderName()); result.setRecipientCount(entity.getRecipientCount());
        result.setPublishTime(entity.getPublishTime()); result.setExpireTime(entity.getExpireTime());
        result.setCreateTime(entity.getCreateTime()); result.setErrorMessage(entity.getErrorMessage());
        return result;
    }

    public InboxMessageCreateNewDataVO createNewData() {
        return new InboxMessageCreateNewDataVO("NORMAL", LocalDateTime.now().plusDays(30));
    }

    @BizLog(value = "保存站内消息草稿", recordRequest = false)
    public Long save(InboxMessageSaveForm form) {
        validateLevel(form.getLevel());
        if (form.getExpireTime().isAfter(LocalDateTime.now().plusDays(365))) {
            throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR,
                    "消息失效时间最多可设置为一年后");
        }
        return txService.save(form);
    }

    @BizLog("发布全站站内消息")
    @AdministratorOnly
    public void publish(InboxMessageVersionForm form) {
        txService.queuePublish(form.id(), form.version());
    }

    @BizLog("重试发布站内消息")
    @AdministratorOnly
    public void retry(InboxMessageVersionForm form) {
        txService.retry(form.id(), form.version());
    }

    /** 调度入口不使用登录态；高风险授权只发生在管理员入队命令边界。 */
    public int dispatchPending(int batchSize) {
        int safeBatchSize = Math.max(1, Math.min(batchSize, 20));
        List<InboxMessageEntity> messages = messageMapper.claimPending(LocalDateTime.now().minusHours(1), safeBatchSize);
        for (InboxMessageEntity message : messages) {
            try {
                txService.distribute(message.getId());
            } catch (Exception exception) {
                String errorMessage = safeErrorMessage(exception);
                log.error("站内消息发布失败: messageId={}", message.getId(), exception);
                txService.markFailed(message.getId(), errorMessage);
            }
        }
        return messages.size();
    }

    private static void validateLevel(String level) {
        if (!LEVELS.contains(level)) {
            throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR, "消息级别不合法");
        }
    }

    private static void validateOptional(String value, Set<String> values, String message) {
        if (value != null && !value.isBlank() && !values.contains(value)) {
            throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR, message);
        }
    }

    private static String safeErrorMessage(Throwable exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) message = exception.getClass().getSimpleName();
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
