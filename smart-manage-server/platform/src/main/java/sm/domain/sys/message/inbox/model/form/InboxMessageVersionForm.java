package sm.domain.sys.message.inbox.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/**
 * 站内消息状态命令参数。
 */
@Schema(description = "站内消息版本命令输入表单")
public record InboxMessageVersionForm(
    @Schema(description = "ID")
    @NotNull(message = "消息ID不能为空") Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "版本号不能为空") Integer version) {
}

