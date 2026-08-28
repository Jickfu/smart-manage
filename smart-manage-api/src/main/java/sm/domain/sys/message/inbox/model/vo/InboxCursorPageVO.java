package sm.domain.sys.message.inbox.model.vo;

import java.util.List;

/** 游标分页结果，避免深分页扫描。 */
public record InboxCursorPageVO(
        List<InboxItemVO> records,
        boolean hasMore,
        String nextCursorTime,
        Long nextCursorMessageId) {
}
