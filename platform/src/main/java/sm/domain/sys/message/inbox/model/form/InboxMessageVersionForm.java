package sm.domain.sys.message.inbox.model.form;

import jakarta.validation.constraints.NotNull;

/** 站内消息状态命令参数。 */
public record InboxMessageVersionForm(
        @NotNull(message = "消息ID不能为空") Long id,
        @NotNull(message = "版本号不能为空") Integer version) {
}

