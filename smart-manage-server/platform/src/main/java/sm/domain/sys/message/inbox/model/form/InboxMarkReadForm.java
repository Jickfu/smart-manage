package sm.domain.sys.message.inbox.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 批量修改当前用户消息已读状态。
 */
@Schema(description = "站内消息收件箱标记已读输入表单")
public record InboxMarkReadForm(
    @Schema(description = "需要标记已读的消息回执键列表")
    @NotEmpty(message = "收件记录不能为空") @Size(max = 100, message = "单次最多处理100条消息") List<@Valid InboxReceiptKeyForm> receipts) {
}

