package sm.domain.sys.message.inbox.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.message.inbox.contract.InboxNotificationCommand;
import sm.domain.sys.message.inbox.mapper.InboxMessageMapper;
import sm.domain.sys.message.inbox.mapper.InboxRecipientMapper;
import sm.domain.sys.message.inbox.model.entity.InboxMessageEntity;
import sm.domain.sys.message.inbox.model.form.InboxMessageSaveForm;
import sm.domain.sys.message.inbox.model.form.InboxReceiptKeyForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class InboxMessageTxService {
    private final InboxMessageMapper messageMapper;
    private final InboxRecipientMapper recipientMapper;
    private final UserMapper userMapper;
    private final CurrentUserContext currentUserContext;

    Long save(InboxMessageSaveForm form) {
        InboxMessageEntity entity;
        if (form.getId() == null) {
            entity = new InboxMessageEntity();
            entity.setSceneKey("admin.broadcast");
            entity.setIdempotencyKey("admin.broadcast:" + UUID.randomUUID());
            entity.setStatus("DRAFT");
            entity.setSenderUserId(currentUserContext.getUserId());
            entity.setSenderName(currentUserContext.getUsernameOrDefault("administrator"));
            entity.setAudienceType("ALL_ENABLED_USERS");
            entity.setRecipientCount(0L);
            entity.setAttemptCount(0);
        } else {
            entity = requireMessage(form.getId());
            requireVersion(entity, form.getVersion());
            if (!"DRAFT".equals(entity.getStatus())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "只有草稿消息可以编辑");
            }
        }
        entity.setTitle(form.getTitle().trim());
        entity.setContent(form.getContent().trim());
        entity.setLevel(form.getLevel());
        entity.setExpireTime(form.getExpireTime());
        int changed = form.getId() == null ? messageMapper.insert(entity) : messageMapper.updateById(entity);
        if (changed != 1) conflict();
        return entity.getId();
    }

    void queuePublish(Long id, Integer version) {
        InboxMessageEntity entity = requireMessage(id);
        requireVersion(entity, version);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "只有草稿消息可以发布");
        }
        if (!entity.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "消息已过期，请修改失效时间后再发布");
        }
        entity.setStatus("PENDING");
        if (messageMapper.updateById(entity) != 1) conflict();
    }

    void retry(Long id, Integer version) {
        InboxMessageEntity entity = requireMessage(id);
        requireVersion(entity, version);
        if (!"FAILED".equals(entity.getStatus())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "只有发布失败的消息可以重试");
        }
        if (!entity.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "消息已过期，不能重试发布");
        }
        entity.setStatus("PENDING");
        entity.setErrorMessage(null);
        if (messageMapper.updateById(entity) != 1) conflict();
    }

    /** 收件快照与发布完成在同一事务提交，用户不会看到半生成的广播。 */
    void distribute(Long messageId) {
        InboxMessageEntity entity = requireMessage(messageId);
        if (!"PUBLISHING".equals(entity.getStatus())) return;
        if (!entity.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("站内消息在派发前已过期");
        }
        LocalDateTime publishTime = LocalDateTime.now();
        int recipientCount = recipientMapper.insertEnabledRecipients(messageId, publishTime);
        if (messageMapper.completePublish(messageId, publishTime, recipientCount) != 1) {
            throw new IllegalStateException("站内消息发布状态已变化");
        }
    }

    void markFailed(Long messageId, String errorMessage) {
        messageMapper.markFailed(messageId, errorMessage);
    }

    void updateReadStatus(Long userId, List<InboxReceiptKeyForm> receipts, boolean readStatus) {
        int changed = recipientMapper.updateReadStatus(userId, receipts, readStatus,
                readStatus ? LocalDateTime.now() : null);
        if (changed != receipts.size()) {
            throw new BizException(ResultEnum.NOT_FOUND, "部分消息不存在或不属于当前用户，请刷新后重试");
        }
    }

    void markAllRead(Long userId, LocalDateTime beginTime) {
        recipientMapper.markAllRead(userId, beginTime, LocalDateTime.now());
    }

    Long publishSystem(InboxNotificationCommand command, List<Long> distinctUserIds) {
        InboxMessageEntity entity = new InboxMessageEntity();
        LocalDateTime publishTime = LocalDateTime.now();
        entity.setSceneKey(command.sceneKey().trim());
        entity.setIdempotencyKey(command.idempotencyKey().trim());
        entity.setTitle(command.title().trim());
        entity.setContent(command.content().trim());
        entity.setLevel(command.level());
        entity.setStatus("PUBLISHED");
        entity.setSenderName("系统通知");
        entity.setAudienceType("USERS");
        entity.setRecipientCount((long) distinctUserIds.size());
        entity.setPublishTime(publishTime);
        entity.setExpireTime(command.expireTime());
        entity.setResourceType(trim(command.resourceType()));
        entity.setResourceId(trim(command.resourceId()));
        entity.setActionCode(trim(command.actionCode()));
        entity.setActionPayload(trim(command.actionPayload()));
        entity.setAttemptCount(0);
        if (messageMapper.insert(entity) != 1) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "创建站内通知失败");
        }
        int inserted = recipientMapper.insertSelectedRecipients(entity.getId(), publishTime, distinctUserIds);
        if (inserted != distinctUserIds.size()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知包含不存在或已停用的接收用户");
        }
        return entity.getId();
    }

    InboxMessageEntity requireMessage(Long id) {
        InboxMessageEntity entity = messageMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "站内消息不存在");
        return entity;
    }

    void validateEnabledUsers(List<Long> userIds) {
        long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .in(UserEntity::getId, userIds).eq(UserEntity::getEnabled, true));
        if (count != userIds.size()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知包含不存在或已停用的接收用户");
        }
    }

    private static void requireVersion(InboxMessageEntity entity, Integer version) {
        if (!Objects.equals(entity.getVersion(), version)) conflict();
    }

    private static String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static void conflict() {
        throw new BizException(ResultEnum.DATA_CONFLICT, "数据已被其他用户修改，请刷新后重试");
    }
}
