package sm.domain.sys.message.inbox.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.message.inbox.contract.InboxNotificationCommand;
import sm.domain.sys.message.inbox.contract.InboxNotificationPublisher;
import sm.domain.sys.message.inbox.mapper.InboxMessageMapper;
import sm.domain.sys.message.inbox.model.entity.InboxMessageEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InboxNotificationService implements InboxNotificationPublisher {
    private static final Set<String> LEVELS = Set.of("NORMAL", "IMPORTANT", "URGENT");
    private final InboxMessageTxService txService;
    private final InboxMessageMapper messageMapper;

    @Override
    public Long publish(InboxNotificationCommand command) {
        validate(command);
        List<Long> userIds = command.recipientUserIds().stream().distinct().toList();
        txService.validateEnabledUsers(userIds);
        try {
            return txService.publishSystem(command, userIds);
        } catch (DuplicateKeyException exception) {
            InboxMessageEntity existing = messageMapper.selectOne(new LambdaQueryWrapper<InboxMessageEntity>()
                    .eq(InboxMessageEntity::getSceneKey, command.sceneKey().trim())
                    .eq(InboxMessageEntity::getIdempotencyKey, command.idempotencyKey().trim()));
            if (existing == null) throw exception;
            return existing.getId();
        }
    }

    private static void validate(InboxNotificationCommand command) {
        if (command == null || !StringUtils.hasText(command.sceneKey())
                || !StringUtils.hasText(command.idempotencyKey())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知场景和幂等键不能为空");
        }
        if (command.sceneKey().trim().length() > 100 || command.idempotencyKey().trim().length() > 200) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知场景或幂等键过长");
        }
        if (command.recipientUserIds() == null || command.recipientUserIds().isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知接收人不能为空");
        }
        if (command.recipientUserIds().size() > 1000) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单条定向站内通知最多包含1000名接收用户");
        }
        if (!StringUtils.hasText(command.title()) || command.title().trim().length() > 200) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知标题不能为空且不能超过200个字符");
        }
        if (!StringUtils.hasText(command.content()) || command.content().trim().length() > 10000) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知正文不能为空且不能超过10000个字符");
        }
        if (!LEVELS.contains(command.level())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知级别不合法");
        }
        if (command.expireTime() == null || !command.expireTime().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知失效时间必须晚于当前时间");
        }
        if (command.expireTime().isAfter(LocalDateTime.now().plusDays(365))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "站内通知失效时间最多可设置为一年后");
        }
        validateOptionalLength(command.resourceType(), 100, "业务资源类型");
        validateOptionalLength(command.resourceId(), 100, "业务资源ID");
        validateOptionalLength(command.actionCode(), 50, "业务动作编码");
        validateOptionalLength(command.actionPayload(), 20_000, "业务动作参数");
    }

    private static void validateOptionalLength(String value, int maxLength, String fieldName) {
        if (StringUtils.hasText(value) && value.trim().length() > maxLength) {
            throw new BizException(ResultEnum.PARAM_ERROR, fieldName + "不能超过" + maxLength + "个字符");
        }
    }
}
