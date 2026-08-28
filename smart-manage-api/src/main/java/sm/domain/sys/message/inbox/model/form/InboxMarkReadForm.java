package sm.domain.sys.message.inbox.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 批量修改当前用户消息已读状态。 */
public record InboxMarkReadForm(
        @NotEmpty(message = "收件记录不能为空")
        @Size(max = 100, message = "单次最多处理100条消息")
        List<@Valid InboxReceiptKeyForm> receipts) {
}

