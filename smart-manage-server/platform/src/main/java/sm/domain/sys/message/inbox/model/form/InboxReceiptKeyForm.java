package sm.domain.sys.message.inbox.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 分区收件记录稳定键。 */
public record InboxReceiptKeyForm(
        @NotNull(message = "消息ID不能为空") Long messageId,
        @NotBlank(message = "收件时间不能为空")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}",
                message = "收件时间键格式不正确")
        String receivedTime) {
}
